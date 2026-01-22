import { useEffect, useState } from "react";
import ChatInput from "./components/ChatInput";
import HeaderBar from "./components/HeaderBar";
import RecommendationCard, {
  RecommendationItem
} from "./components/RecommendationCard";

interface RecommendResponse {
  items: RecommendationItem[];
  globalNote?: string;
  followUpQuestion?: string | null;
}

const STORE_NAME = "모묵 식당";
const TABLE_LABEL = "테이블 A-3";

const App = () => {
  const [message, setMessage] = useState("");
  const [items, setItems] = useState<RecommendationItem[]>([]);
  const [globalNote, setGlobalNote] = useState<string | undefined>();
  const [followUpQuestion, setFollowUpQuestion] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    document.title = "모묵 | 식당 QR 추천";
  }, []);

  const requestRecommendations = async () => {
    if (!message.trim()) {
      return;
    }

    setIsLoading(true);
    setErrorMessage(null);

    try {
      const response = await fetch("/public/recommendations", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          userText: message.trim(),
          topN: 5
        })
      });

      if (!response.ok) {
        throw new Error("추천 API 호출에 실패했습니다.");
      }

      const data: RecommendResponse = await response.json();
      setItems(data.items ?? []);
      setGlobalNote(data.globalNote);
      setFollowUpQuestion(data.followUpQuestion ?? null);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "추천 결과를 불러오지 못했습니다."
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-6">
      <div className="mx-auto flex w-full max-w-md flex-col gap-5">
        <HeaderBar storeName={STORE_NAME} tableLabel={TABLE_LABEL} />

        <ChatInput
          value={message}
          onChange={setMessage}
          onSubmit={requestRecommendations}
          isLoading={isLoading}
        />

        <section className="rounded-2xl bg-white p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-semibold text-slate-800">추천 결과</h2>
            <span className="text-xs text-slate-400">3~5개 카드</span>
          </div>

          {errorMessage ? (
            <p className="mt-3 text-sm text-rose-500">{errorMessage}</p>
          ) : null}

          {globalNote ? (
            <p className="mt-3 text-sm text-emerald-600">{globalNote}</p>
          ) : null}

          {followUpQuestion ? (
            <p className="mt-2 text-xs text-slate-500">{followUpQuestion}</p>
          ) : null}

          {items.length === 0 && !isLoading ? (
            <p className="mt-4 text-sm text-slate-400">
              아직 추천 결과가 없습니다. 메시지를 보내면 카드가 표시됩니다.
            </p>
          ) : null}

          <div className="mt-4 flex flex-col gap-4">
            {items.map((item, index) => (
              <RecommendationCard key={`${item.menu_id ?? item.menuId ?? index}`} item={item} />
            ))}
          </div>
        </section>
      </div>
    </div>
  );
};

export default App;
