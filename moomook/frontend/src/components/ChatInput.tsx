import { FormEvent } from "react";

interface ChatInputProps {
  value: string;
  onChange: (value: string) => void;
  onSubmit: () => void;
  isLoading: boolean;
}

const ChatInput = ({ value, onChange, onSubmit, isLoading }: ChatInputProps) => {
  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    onSubmit();
  };

  return (
    <form onSubmit={handleSubmit} className="rounded-2xl bg-white p-4 shadow-sm">
      <label className="text-sm font-semibold text-slate-700">
        오늘 어떤 메뉴가 끌리나요?
      </label>
      <div className="mt-3 flex gap-2">
        <input
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder="예: 매운 거 땡기고 혼밥이야"
          className="flex-1 rounded-xl border border-slate-200 px-4 py-3 text-sm focus:border-emerald-400 focus:outline-none"
        />
        <button
          type="submit"
          disabled={isLoading || value.trim().length === 0}
          className="rounded-xl bg-emerald-500 px-4 py-3 text-sm font-semibold text-white shadow-sm transition disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {isLoading ? "전송 중" : "전송"}
        </button>
      </div>
    </form>
  );
};

export default ChatInput;
