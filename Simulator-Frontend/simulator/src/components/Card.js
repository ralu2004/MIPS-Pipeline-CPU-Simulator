export default function Card({ title, children }) {
  return (
    <div className="bg-slate-800/50 backdrop-blur rounded-xl p-6 border border-slate-700 shadow-lg">
      <h2 className="text-xl font-bold mb-4 text-purple-300">{title}</h2>
      {children}
    </div>
  );
}