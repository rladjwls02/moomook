# SPEC.md — QR 기반 메뉴 추천 AI (사장님 등록 메뉴 한정)

## 0. 문서 목적
- QR로 접속한 손님이 채팅으로 “기분/취향/상황”을 입력하면, **사장님이 미리 등록해둔 메뉴 데이터 안에서만** 추천을 제공한다.
- LLM은 **조건 추출 + 추천 이유 생성**만 담당하고, 실제 추천 후보 선택은 **서버 점수화 엔진**이 담당하여 환각(없는 메뉴 추천)을 원천 차단한다.

---

## 1. 용어 정의
- **손님(Client)**: QR로 웹에 접속해 추천을 받는 사용자
- **사장님(Admin)**: 메뉴를 등록/수정/품절 처리하는 사용자
- **메뉴 카탈로그(Menu Catalog)**: 사장님이 등록한 메뉴 DB
- **조건(Preference Constraints)**: 손님 입력을 구조화한 추천 조건 JSON
- **추천 엔진(Scoring Engine)**: DB 메뉴를 필터링/점수화해 Top-N을 산출하는 서버 로직
- **LLM**: 손님 입력 → 조건 JSON 추출, 추천 결과 → 자연어 설명 생성

---

## 2. 목표 / 비목표
### 2.1 목표 (MVP)
1) 손님: QR 접속 → 채팅 입력 → Top 3~5 메뉴 추천 + 이유 제공  
2) 사장님: 메뉴 CRUD, 품절/판매중 토글, 태그/매움/조리시간/알레르기 관리  
3) 환각 방지: **추천 결과는 반드시 DB의 메뉴 id 목록에서만 생성**  
4) 로그: 추천 요청/응답/선택(클릭) 이벤트 저장

### 2.2 비목표 (MVP에서는 제외)
- 결제/배달/포장 시스템 연동
- 실시간 재고/주방 디스플레이(KDS)
- 복잡한 개인화(장기 학습) / 협업 필터링
- 멀티 매장 프랜차이즈급 권한/정산/회계

---

## 3. 사용자 시나리오
### 3.1 손님 시나리오
1) 테이블 QR 스캔 → `/{storeSlug}/t/{tableCode}` 접속
2) “오늘 어떤 느낌?” 입력  
   예: “매운 거 땡기고 혼밥이야. 빨리 먹고 싶어”
3) (선택) 제외 재료/알레르기/예산/매움 정도 선택
4) 추천 결과 3~5개 표시
5) “이 중에서 더 가볍게”, “국물로”, “덜 매워” 같은 후속 채팅으로 재추천

### 3.2 사장님 시나리오
1) 관리자 로그인
2) 메뉴 등록: 이름/가격/설명/태그/매움/조리시간/알레르기
3) 품절 처리 → 손님 추천에서 즉시 제외
4) 추천 우선순위(가중치) 조정(선택)

---

## 4. 기능 요구사항 (FR)
### 4.1 공통
- [FR-001] 모든 추천은 **해당 매장(store)** 의 **판매중 메뉴(is_available=true)** 에서만 나온다.
- [FR-002] 알레르기/제외 재료 조건은 필터에서 최우선으로 적용한다.
- [FR-003] 추천 응답에는 항상 `menu_id`가 포함된다(문자열/숫자).

### 4.2 손님(웹)
- [FR-101] QR 링크는 매장/테이블 식별자를 포함한다.
- [FR-102] 손님은 채팅으로 요구사항을 입력할 수 있다.
- [FR-103] 시스템은 손님 입력을 **조건 JSON** 으로 구조화한다(LLM).
- [FR-104] 시스템은 조건 JSON 기반으로 Top-N 메뉴를 산출한다(서버).
- [FR-105] 시스템은 추천 이유를 자연어로 제공한다(LLM).
- [FR-106] 손님은 추천 결과를 클릭/선택할 수 있고, 시스템은 선택 로그를 저장한다.

### 4.3 사장님(관리자)
- [FR-201] 메뉴 CRUD
- [FR-202] 메뉴 품절/판매중 토글
- [FR-203] 메뉴 태그 관리(멀티 선택)
- [FR-204] 메뉴 속성 관리: 매움(0~3), 조리시간(분), 알레르기(멀티)
- [FR-205] (선택) 메뉴 우선순위 점수(priority_score) 설정

### 4.4 운영/로그
- [FR-301] 추천 요청 로그 저장(입력 텍스트, 추출 조건 JSON, 추천 결과 ids, 모델 메타)
- [FR-302] 추천 클릭/선택 로그 저장(세션/테이블/시간/메뉴 id)
- [FR-303] 에러 로그(LLM 실패, 파싱 실패 등) 기록

---

## 5. 비기능 요구사항 (NFR)
- [NFR-001] 응답 시간: 추천 결과 화면 렌더링까지 목표 2초 내(LLM 포함; 캐시/폴백 허용)
- [NFR-002] 안정성: LLM 실패 시에도 서버는 규칙 기반 폴백 추천 제공
- [NFR-003] 보안:
  - 관리자 인증/인가 필수
  - 테이블 링크는 추측 난이도 있는 코드 사용(랜덤/서명 토큰)
- [NFR-004] 확장성: 매장 단위 멀티테넌시(store_id)
- [NFR-005] 관측성: 추천 전환율/클릭률을 계산할 수 있도록 로그 구조화

---

## 6. 시스템 아키텍처 (권장)
### 6.1 추천 파이프라인(환각 방지)
1) 손님 입력 수신
2) LLM 호출: 입력 → 조건 JSON 추출
3) 서버 필터링: is_available, allergens/excludes, budget, etc.
4) 서버 점수화: tags match + priority + popularity + cook_time penalty + spicy match
5) Top-N 메뉴 id 리스트 생성
6) LLM 호출: Top-N + 조건 → 자연어 추천 설명 생성  
   - **설명은 제공된 Top-N 메뉴만 언급**

### 6.2 폴백 정책
- LLM 조건 추출 실패:
  - 기본값 조건으로 점수화(“대표/인기 + 조리시간 빠른 것” 우선)
  - 추가 질문 1개만(예: “매운 거 괜찮아?”)
- LLM 설명 생성 실패:
  - 서버 템플릿으로 이유 생성(태그 기반 문장)

---

## 7. 데이터 모델(초안)
### 7.1 Store
- id (PK)
- name
- slug (unique)
- created_at, updated_at

### 7.2 Table
- id (PK)
- store_id (FK)
- code (unique per store)
- display_name
- created_at, updated_at

### 7.3 Menu
- id (PK)
- store_id (FK)
- name
- price (int)
- description (varchar)
- tags (json array or 별도 테이블)
- spicy_level (0~3)
- cook_time_min (int)
- allergens (json array or 별도 테이블)
- is_available (boolean)
- priority_score (int, default 0)
- popularity_score (int, default 0; 로그로 추후 계산 가능)
- created_at, updated_at

### 7.4 RecommendationSession (선택/권장)
- id (PK)
- store_id (FK)
- table_id (FK, nullable)
- session_key (cookie or uuid)
- created_at

### 7.5 RecommendationLog
- id (PK)
- store_id (FK)
- table_id (FK, nullable)
- session_key
- user_text (text)
- extracted_constraints (json)
- recommended_menu_ids (json array)
- model_provider (varchar)
- model_name (varchar)
- latency_ms (int)
- created_at

### 7.6 RecommendationClickLog
- id (PK)
- store_id (FK)
- table_id (FK, nullable)
- session_key
- menu_id (FK)
- action (click/select)
- created_at

---

## 8. 조건 JSON 스키마 (LLM 출력 포맷)
### 8.1 Constraints JSON (예시)

OUTPUT_SCHEMA.json
```json
{
  "spicy_preference": "any",
  "spicy_min": 0,
  "spicy_max": 3,
  "desired_tags": ["혼밥", "든든"],
  "avoid_tags": ["기름진"],
  "exclude_ingredients": ["마늘"],
  "allergens": ["견과"],
  "max_cook_time_min": 15,
  "budget_max": 12000,
  "portion_preference": "any",
  "notes": "빨리 먹고 싶음"
}
{
  "version": "1.0.0",
  "schemas": {
    "extract_constraints": {
      "type": "object",
      "additionalProperties": false,
      "required": ["constraints", "followUpQuestion"],
      "properties": {
        "constraints": {
          "type": "object",
          "additionalProperties": false,
          "required": [
            "spicy_preference",
            "spicy_min",
            "spicy_max",
            "desired_tags",
            "avoid_tags",
            "exclude_ingredients",
            "allergens",
            "max_cook_time_min",
            "budget_max",
            "portion_preference",
            "notes"
          ],
          "properties": {
            "spicy_preference": {
              "type": "string",
              "enum": ["any", "mild", "medium", "hot"]
            },
            "spicy_min": { "type": "integer", "minimum": 0, "maximum": 3 },
            "spicy_max": { "type": "integer", "minimum": 0, "maximum": 3 },

            "desired_tags": {
              "type": "array",
              "items": { "type": "string" },
              "maxItems": 10
            },
            "avoid_tags": {
              "type": "array",
              "items": { "type": "string" },
              "maxItems": 10
            },

            "exclude_ingredients": {
              "type": "array",
              "items": { "type": "string" },
              "maxItems": 10
            },
            "allergens": {
              "type": "array",
              "items": { "type": "string" },
              "maxItems": 10
            },

            "max_cook_time_min": {
              "type": ["integer", "null"],
              "minimum": 1,
              "maximum": 120
            },
            "budget_max": {
              "type": ["integer", "null"],
              "minimum": 1000,
              "maximum": 300000
            },

            "portion_preference": {
              "type": "string",
              "enum": ["any", "light", "filling"]
            },
            "notes": { "type": "string", "maxLength": 200 }
          }
        },
        "followUpQuestion": {
          "type": ["string", "null"],
          "maxLength": 80,
          "description": "불확실할 때만 질문 1개. 아니면 null."
        }
      }
    },

    "generate_explanations": {
      "type": "object",
      "additionalProperties": false,
      "required": ["items", "globalNote"],
      "properties": {
        "items": {
          "type": "array",
          "minItems": 1,
          "maxItems": 5,
          "items": {
            "type": "object",
            "additionalProperties": false,
            "required": ["menuId", "reason"],
            "properties": {
              "menuId": { "type": "integer" },
              "reason": { "type": "string", "maxLength": 180 }
            }
          }
        },
        "globalNote": {
          "type": "string",
          "maxLength": 200,
          "description": "전체적으로 한 줄 코멘트(선택이지만 고정 필드로 둠)."
        }
      }
    }
  }
}
```
