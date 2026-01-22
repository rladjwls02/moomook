export interface RecommendationItem {
  menu_id?: number;
  menuId?: number;
  name: string;
  price: number;
  tags?: string[] | string;
  cook_time_min?: number;
  cookTimeMin?: number;
  reason: string;
}

interface RecommendationCardProps {
  item: RecommendationItem;
}

const RecommendationCard = ({ item }: RecommendationCardProps) => {
  const tags = Array.isArray(item.tags)
    ? item.tags
    : typeof item.tags === "string"
      ? item.tags.split(",").map((tag) => tag.trim()).filter(Boolean)
      : [];
  const cookTime = item.cook_time_min ?? item.cookTimeMin;

  return (
    <article className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <h3 className="text-lg font-semibold text-slate-900">{item.name}</h3>
          <p className="mt-1 text-sm font-medium text-emerald-600">
            {item.price.toLocaleString()}원
          </p>
        </div>
        <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
          {cookTime ? `${cookTime}분` : "조리시간 미제공"}
        </span>
      </div>
      <div className="mt-3 flex flex-wrap gap-2">
        {(tags.length > 0 ? tags : ["추천 메뉴"]).map((tag) => (
          <span
            key={tag}
            className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-600"
          >
            #{tag}
          </span>
        ))}
      </div>
      <p className="mt-3 text-sm text-slate-600">{item.reason}</p>
    </article>
  );
};

export default RecommendationCard;
