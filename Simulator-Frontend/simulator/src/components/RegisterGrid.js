import {REGISTER_NAMES} from './Constants';
import React, { useEffect, useRef, useState } from 'react';

const REGISTER_INFO = {
  '$zero': 'Constant value 0 (read-only)',
  '$at': 'Assembler temporary',
  '$v0': 'Function return value 0',
  '$v1': 'Function return value 1',
  '$a0': 'Function argument 0',
  '$a1': 'Function argument 1',
  '$a2': 'Function argument 2',
  '$a3': 'Function argument 3',
  '$t0': 'Temporary register 0',
  '$t1': 'Temporary register 1',
  '$t2': 'Temporary register 2',
  '$t3': 'Temporary register 3',
  '$t4': 'Temporary register 4',
  '$t5': 'Temporary register 5',
  '$t6': 'Temporary register 6',
  '$t7': 'Temporary register 7',
  '$s0': 'Saved register 0 (preserved)',
  '$s1': 'Saved register 1 (preserved)',
  '$s2': 'Saved register 2 (preserved)',
  '$s3': 'Saved register 3 (preserved)',
  '$s4': 'Saved register 4 (preserved)',
  '$s5': 'Saved register 5 (preserved)',
  '$s6': 'Saved register 6 (preserved)',
  '$s7': 'Saved register 7 (preserved)',
  '$t8': 'Temporary register 8',
  '$t9': 'Temporary register 9',
  '$k0': 'Kernel/OS reserved 0',
  '$k1': 'Kernel/OS reserved 1',
  '$gp': 'Global pointer',
  '$sp': 'Stack pointer',
  '$fp': 'Frame pointer',
  '$ra': 'Return address'
};

export default function RegisterGrid({ registers }) {
  const registerArray = Array.isArray(registers) 
    ? registers 
    : Object.values(registers);
  
  return (
    <div className="grid grid-cols-2 gap-2 max-h-[400px] overflow-y-auto custom-scrollbar">
      {registerArray.map((value, index) => (
        <RegisterDisplay 
          key={index} 
          name={REGISTER_NAMES[index]} 
          value={value}
          index={index}
        />
      ))}
    </div>
  );
}
/*
function RegisterDisplay({ name, value, index }) {
  const isZero = index === 0;
  const hasValue = value !== 0 || isZero;
  const info = REGISTER_INFO[name] || 'General purpose register';
  
  return (
    <div className={`bg-slate-900 rounded p-2 border transition-all relative group ${
      hasValue ? 'border-purple-500/50' : 'border-slate-700'
    }`}>
      <div className="text-xs text-purple-400 font-bold">{name}</div>
      <div className="font-mono text-sm text-slate-300">{value}</div>
      <div className="text-xs text-slate-500 font-mono">
        0x{value.toString(16).toUpperCase().padStart(8, '0')}
      </div>
      
      <div className="absolute left-0 top-full mt-2 w-48 bg-slate-800 border border-purple-500 rounded-lg p-2 text-xs text-slate-300 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-10 shadow-xl">
        <div className="font-bold text-purple-400 mb-1">{name}</div>
        {info}
      </div>
    </div>
  );
}*/
function RegisterDisplay({ name, value, index }) {
  const [highlight, setHighlight] = useState(false);
  const prevValueRef = useRef(value);
  const timeoutRef = useRef(null);

  const isZero = index === 0;
  const hasValue = value !== 0 || isZero;
  const info = REGISTER_INFO[name] || 'General purpose register';

  useEffect(() => {
    // Skip $zero register as it never changes
    if (index === 0) return;

    // Check if value has changed
    if (value !== prevValueRef.current) {
      // Remove any existing timeout
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }

      // Set highlight to true
      setHighlight(true);

      // Set timeout to remove highlight after 1 second
      timeoutRef.current = setTimeout(() => {
        setHighlight(false);
      }, 1000);

      // Update previous value reference
      prevValueRef.current = value;
    }

    // Cleanup timeout on unmount
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