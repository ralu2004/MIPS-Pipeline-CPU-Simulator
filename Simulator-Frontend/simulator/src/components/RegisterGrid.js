import {REGISTER_NAMES, REGISTER_INFO} from './Constants';
import React, { useEffect, useRef, useState, useMemo } from 'react';

export default function RegisterGrid({ registers }) {
  const [searchTerm, setSearchTerm] = useState('');
  const [showNonZero, setShowNonZero] = useState(false);

  const registerArray = Array.isArray(registers) 
    ? registers 
    : Object.values(registers);

  const filteredRegisters = useMemo(() => {
    let filtered = registerArray.map((value, index) => ({
      index,
      name: REGISTER_NAMES[index],
      value
    }));

    if (showNonZero) {
      filtered = filtered.filter(r => r.value !== 0 || r.index === 0);
    }

    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(r => {
        const nameMatch = r.name.toLowerCase().includes(searchLower);
        const indexMatch = r.index.toString().includes(searchTerm);
        const valueMatch = r.value.toString().includes(searchTerm) || 
                         r.value.toString(16).toUpperCase().includes(searchTerm.toUpperCase());
        return nameMatch || indexMatch || valueMatch;
      });
    }

    return filtered;
  }, [registerArray, showNonZero, searchTerm]);
  
  return (
    <div>
      <div className="flex gap-2 mb-3 flex-wrap">
        <input
          type="text"
          placeholder="Search by name, index, or value"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
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

      {filteredRegisters.length === 0 ? (
        <div className="text-slate-400 text-sm text-center py-4">
          {showNonZero ? 'No non-zero registers found' : 'No matching registers found'}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-2 max-h-[400px] overflow-y-auto custom-scrollbar">
          {filteredRegisters.map(({ index, name, value }) => (
            <RegisterDisplay 
              key={index} 
              name={name} 
              value={value}
              index={index}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function RegisterDisplay({ name, value, index }) {
  const [highlight, setHighlight] = useState(false);
  const prevValueRef = useRef(value);
  const timeoutRef = useRef(null);

  const isZero = index === 0;
  const hasValue = value !== 0 || isZero;
  const info = REGISTER_INFO[name] || 'General purpose register';

  useEffect(() => {
    if (index === 0) return;

    if (value !== prevValueRef.current) {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }

      setHighlight(true);

      timeoutRef.current = setTimeout(() => {
        setHighlight(false);
      }, 1000);

      prevValueRef.current = value;
    }

    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, [value, index]);

  return (
    <div className={`
      bg-slate-900 rounded p-2 border transition-all duration-300 relative group
      ${highlight ? 'ring-4 ring-green-500 ring-opacity-50 border-green-400' : ''}
      ${hasValue && !highlight ? 'border-purple-500/50' : 'border-slate-700'}
      ${highlight ? 'shadow-lg shadow-green-500/20' : ''}
    `}>
      <div className="text-xs text-purple-400 font-bold">{name}</div>
      <div className={`font-mono text-sm transition-colors duration-300 ${
        highlight ? 'text-green-300' : 'text-slate-300'
      }`}>{value}</div>
      <div className={`text-xs font-mono transition-colors duration-300 ${
        highlight ? 'text-green-400/70' : 'text-slate-500'
      }`}>
        0x{value.toString(16).toUpperCase().padStart(8, '0')}
      </div>
      
      <div className="absolute left-0 top-full mt-2 w-48 bg-slate-800 border border-purple-500 rounded-lg p-2 text-xs text-slate-300 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-10 shadow-xl">
        <div className="font-bold text-purple-400 mb-1">{name}</div>
        {info}
      </div>
    </div>
  );
}