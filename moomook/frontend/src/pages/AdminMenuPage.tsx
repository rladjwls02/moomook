import { useEffect, useMemo, useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import {
  AdminMenu,
  AdminMenuRequest,
  createMenu,
  deleteMenu,
  listMenus,
  updateMenu
} from "../services/adminMenuApi";

const STORE_ID = 1;

const emptyForm = {
  name: "",
  price: "",
  description: "",
  tags: "",
  spicyLevel: "0",
  cookTimeMin: "0",
  allergens: "",
  ingredients: "",
  isAvailable: true,
  priorityScore: "0",
  popularityScore: "0"
};

type FormState = typeof emptyForm;

const toList = (value: string) =>
  value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);

const AdminMenuPage = () => {
  const [menus, setMenus] = useState<AdminMenu[]>([]);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [selectedMenuId, setSelectedMenuId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const formTitle = useMemo(
    () => (selectedMenuId ? "메뉴 수정" : "메뉴 추가"),
    [selectedMenuId]
  );

  const resetForm = () => {
    setForm(emptyForm);
    setSelectedMenuId(null);
  };

  const refreshMenus = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await listMenus(STORE_ID);
      setMenus(data);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "메뉴 목록을 불러오지 못했습니다."
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    document.title = "모묵 | 사장님 메뉴 관리";
    refreshMenus();
  }, []);

  const handleChange = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = event.target;

    if (type === "checkbox") {
      const checkbox = event.target as HTMLInputElement;
      setForm((prev) => ({ ...prev, [name]: checkbox.checked }));
      return;
    }

    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    const payload: AdminMenuRequest = {
      name: form.name.trim(),
      price: Number(form.price) || 0,
      description: form.description.trim(),
      tags: toList(form.tags),
      spicyLevel: Number(form.spicyLevel) || 0,
      cookTimeMin: Number(form.cookTimeMin) || 0,
      allergens: toList(form.allergens),
      ingredients: toList(form.ingredients),
      isAvailable: form.isAvailable,
      priorityScore: Number(form.priorityScore) || 0
    };

    try {
      if (selectedMenuId) {
        await updateMenu(STORE_ID, selectedMenuId, payload);
        setSuccessMessage("메뉴가 수정되었습니다.");
      } else {
        await createMenu(STORE_ID, payload);
        setSuccessMessage("메뉴가 추가되었습니다.");
      }
      resetForm();
      refreshMenus();
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "메뉴 저장에 실패했습니다."
      );
    }
  };

  const handleEdit = (menu: AdminMenu) => {
    setSelectedMenuId(menu.id);
    setForm({
      name: menu.name,
      price: String(menu.price),
      description: menu.description ?? "",
      tags: menu.tags.join(", "),
      spicyLevel: String(menu.spicyLevel),
      cookTimeMin: String(menu.cookTimeMin),
      allergens: menu.allergens.join(", "),
      ingredients: menu.ingredients.join(", "),
      isAvailable: menu.isAvailable,
      priorityScore: String(menu.priorityScore),
      popularityScore: String(menu.popularityScore ?? 0)
    });
  };

  const handleDelete = async (menuId: number) => {
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      await deleteMenu(STORE_ID, menuId);
      setSuccessMessage("메뉴가 삭제되었습니다.");
      refreshMenus();
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "메뉴 삭제에 실패했습니다."
      );
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-6">
      <div className="mx-auto flex w-full max-w-5xl flex-col gap-6">
        <header className="rounded-2xl bg-white p-5 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <p className="text-sm text-slate-500">사장님용</p>
              <h1 className="text-2xl font-semibold text-slate-900">메뉴 관리</h1>
            </div>
            <div className="text-sm text-slate-500">
              매장 ID: <span className="font-semibold text-slate-700">{STORE_ID}</span>
            </div>
          </div>
          <p className="mt-3 text-sm text-slate-500">
            메뉴 목록을 관리하고 추천 우선순위를 조정하세요.
          </p>
        </header>

        <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
          <section className="rounded-2xl bg-white p-5 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-800">등록된 메뉴</h2>
              {isLoading ? (
                <span className="text-xs text-slate-400">불러오는 중...</span>
              ) : null}
            </div>

            {errorMessage ? (
              <p className="mt-3 text-sm text-rose-500">{errorMessage}</p>
            ) : null}

            {successMessage ? (
              <p className="mt-3 text-sm text-emerald-600">{successMessage}</p>
            ) : null}

            <div className="mt-4 space-y-4">
              {menus.length === 0 && !isLoading ? (
                <p className="text-sm text-slate-400">
                  아직 등록된 메뉴가 없습니다. 오른쪽 폼에서 메뉴를 추가해보세요.
                </p>
              ) : null}

              {menus.map((menu) => (
                <div
                  key={menu.id}
                  className="rounded-xl border border-slate-100 bg-slate-50 p-4"
                >
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div>
                      <h3 className="text-base font-semibold text-slate-800">
                        {menu.name}
                      </h3>
                      <p className="text-sm text-slate-500">
                        {menu.description || "메뉴 설명이 없습니다."}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-semibold text-slate-700">
                        {menu.price.toLocaleString()}원
                      </p>
                      <p className="text-xs text-slate-400">
                        인기도 {menu.popularityScore}
                      </p>
                    </div>
                  </div>

                  <div className="mt-3 flex flex-wrap gap-2 text-xs text-slate-500">
                    <span className="rounded-full bg-white px-2 py-1">
                      매운맛 {menu.spicyLevel}
                    </span>
                    <span className="rounded-full bg-white px-2 py-1">
                      조리 {menu.cookTimeMin}분
                    </span>
                    <span className="rounded-full bg-white px-2 py-1">
                      우선순위 {menu.priorityScore}
                    </span>
                    <span className="rounded-full bg-white px-2 py-1">
                      {menu.isAvailable ? "판매 중" : "판매 중지"}
                    </span>
                  </div>

                  <div className="mt-4 flex flex-wrap gap-3">
                    <button
                      type="button"
                      className="rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-semibold text-white"
                      onClick={() => handleEdit(menu)}
                    >
                      수정
                    </button>
                    <button
                      type="button"
                      className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600"
                      onClick={() => handleDelete(menu.id)}
                    >
                      삭제
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </section>

          <section className="rounded-2xl bg-white p-5 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-800">{formTitle}</h2>
              {selectedMenuId ? (
                <button
                  type="button"
                  className="text-xs font-semibold text-slate-500"
                  onClick={resetForm}
                >
                  새 메뉴 입력
                </button>
              ) : null}
            </div>
            <form className="mt-4 space-y-4" onSubmit={handleSubmit}>
              <div>
                <label className="text-xs font-semibold text-slate-500">메뉴 이름</label>
                <input
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                  placeholder="예: 모묵 특제 우동"
                  required
                />
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="text-xs font-semibold text-slate-500">가격</label>
                  <input
                    name="price"
                    type="number"
                    min="0"
                    value={form.price}
                    onChange={handleChange}
                    className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                    placeholder="12000"
                    required
                  />
                </div>
                <div>
                  <label className="text-xs font-semibold text-slate-500">우선순위 점수</label>
                  <input
                    name="priorityScore"
                    type="number"
                    min="0"
                    value={form.priorityScore}
                    onChange={handleChange}
                    className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-500">설명</label>
                <textarea
                  name="description"
                  value={form.description}
                  onChange={handleChange}
                  className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                  rows={3}
                  placeholder="메뉴 소개를 적어주세요."
                />
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="text-xs font-semibold text-slate-500">태그</label>
                  <input
                    name="tags"
                    value={form.tags}
                    onChange={handleChange}
                    className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                    placeholder="매콤, 인기"
                  />
                </div>
                <div>
                  <label className="text-xs font-semibold text-slate-500">알레르기</label>
                  <input
                    name="allergens"
                    value={form.allergens}
                    onChange={handleChange}
                    className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                    placeholder="땅콩, 우유"
                  />
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="text-xs font-semibold text-slate-500">매운맛 단계</label>
                  <input
                    name="spicyLevel"
                    type="number"
                    min="0"
                    value={form.spicyLevel}
                    onChange={handleChange}
                    className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="text-xs font-semibold text-slate-500">조리시간(분)</label>
                  <input
                    name="cookTimeMin"
                    type="number"
                    min="0"
                    value={form.cookTimeMin}
                    onChange={handleChange}
                    className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                  />
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="text-xs font-semibold text-slate-500">재료</label>
                  <input
                    name="ingredients"
                    value={form.ingredients}
                    onChange={handleChange}
                    className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                    placeholder="닭고기, 파"
                  />
                </div>
                <div>
                  <label className="text-xs font-semibold text-slate-500">
                    인기도 (읽기 전용)
                  </label>
                  <input
                    name="popularityScore"
                    value={form.popularityScore}
                    className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-400"
                    readOnly
                  />
                </div>
              </div>

              <label className="flex items-center gap-2 text-sm text-slate-600">
                <input
                  name="isAvailable"
                  type="checkbox"
                  checked={form.isAvailable}
                  onChange={handleChange}
                  className="h-4 w-4 rounded border-slate-300"
                />
                판매 중
              </label>

              <button
                type="submit"
                className="w-full rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
              >
                {selectedMenuId ? "메뉴 수정하기" : "메뉴 추가하기"}
              </button>
            </form>
          </section>
        </div>
      </div>
    </div>
  );
};

export default AdminMenuPage;
