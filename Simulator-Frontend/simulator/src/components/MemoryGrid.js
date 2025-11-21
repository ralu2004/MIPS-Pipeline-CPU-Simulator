export default function MemoryGrid({ memory }) {
  const memoryEntries = Array.isArray(memory)
    ? memory.map((value, addr) => ({ addr, value })).filter(e => e.value !== 0)
    : Object.entries(memory).map(([addr, value]) => ({ 
        addr: parseInt(addr), 
        value 
      })).filter(e => e.value !== 0);

  if (memoryEntries.length === 0) {
    return <div className="text-slate-400 text-sm text-center py-4">No data in memory</div>;
  }

  return (
    <div className="grid grid-cols-2 gap-2 max-h-[400px] overflow-y-auto custom-scrollbar">
      {memoryEntries.slice(0, 32).map(({ addr, value }) => (
        <div key={addr} className="bg-slate-900 rounded p-2 border border-purple-500/50">
          <div className="text-xs text-purple-400 font-bold font-mono">
            [0x{addr.toString(16).toUpperCase().padStart(4, '0')}]
          </div>
          <div className="font-mono text-sm text-slate-300">{value}</div>
          <div className="text-xs text-slate-500 font-mono">
            0x{value.toString(16).toUpperCase().padStart(8, '0')}
          </div>
        </div>
      ))}
    </div>
  );
}