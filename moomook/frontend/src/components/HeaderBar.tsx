interface HeaderBarProps {
  storeName: string;
  tableLabel: string;
}

const HeaderBar = ({ storeName, tableLabel }: HeaderBarProps) => {
  return (
    <header className="rounded-2xl bg-white px-5 py-4 shadow-sm">
      <p className="text-xs font-semibold uppercase tracking-widest text-emerald-500">
        식당 QR 서비스
      </p>
      <div className="mt-2 flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900">{storeName}</h1>
          <p className="text-sm text-slate-500">{tableLabel}</p>
        </div>
        <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-600">
          추천 MVP
        </span>
      </div>
    </header>
  );
};

export default HeaderBar;
