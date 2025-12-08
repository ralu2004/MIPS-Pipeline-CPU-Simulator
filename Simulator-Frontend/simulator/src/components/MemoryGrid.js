import React, { useState, useMemo } from 'react';

export default function MemoryGrid({ memory }) {
  const [showNonZero, setShowNonZero] = useState(false);
  const [searchAddr, setSearchAddr] = useState('');

  const memoryEntries = useMemo(() => {
    const entries = Array.isArray(memory)
      ? memory.map((value, addr) => ({ addr, value }))
      : Object.entries(memory).map(([addr, value]) => ({ 
          addr: parseInt(addr), 
          value 
        }));

    let filtered = entries;
    
    if (showNonZero) {
      filtered = filtered.filter(e => e.value !== 0);
    }

    if (searchAddr.trim()) {
      const searchNum = parseInt(searchAddr, 16) || parseInt(searchAddr, 10);
      if (!isNaN(searchNum)) {
        filtered = filtered.filter(e => 
          e.addr === searchNum || 
          e.addr.toString(16).toUpperCase().includes(searchAddr.toUpperCase())
        );
      }
    }

    return filtered;
  }, [memory, showNonZero, searchAddr]);

  if (!memory || (Array.isArray(memory) && memory.length === 0) || Object.keys(memory).length === 0) {
    return <div className="text-slate-400 text-sm text-center py-4">No data in memory</div>;
  }

  return (
    <div>
      <div className="flex gap-2 mb-3 flex-wrap">
        <input
          type="text"
          placeholder="Search address (hex or decimal)"
          value={searchAddr}
          onChange={(e) => setSearchAddr(e.target.value)}
          className="flex-1 min-w-[150px] bg-slate-800 border border-slate-600 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-purple-500"
        />
        <button
          onClick={() => setShowNonZero(!showNonZero)}
          className={`px-3 py-1.5 rounded text-sm font-semibold transition-all ${
            showNonZero
              ? 'bg-purple-600 text-white'
              : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
          }`}
        >
          {showNonZero ? 'Show All' : 'Non-Zero Only'}
        </button>
      </div>
      
      {memoryEntries.length === 0 ? (
        <div className="text-slate-400 text-sm text-center py-4">
          {showNonZero ? 'No non-zero values found' : 'No matching addresses found'}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-2 max-h-[400px] overflow-y-auto custom-scrollbar">
          {memoryEntries.slice(0, 64).map(({ addr, value }) => (
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
          {memoryEntries.length > 64 && (
            <div className="col-span-2 text-xs text-slate-400 text-center py-2">
              Showing first 64 of {memoryEntries.length} entries
            </div>
          )}
        </div>
      )}
    </div>
  );
}