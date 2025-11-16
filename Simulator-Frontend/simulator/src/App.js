import React, { useState, useEffect, useCallback } from 'react';
import { Play, Pause, RotateCcw, Upload, Cpu, AlertCircle, CheckCircle, StepForward, RefreshCw } from 'lucide-react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:85';

// Sample MIPS programs in hex format
const SAMPLE_PROGRAMS = {
  'Simple ADD': {
    code: '00221820\n00832022\n00a42824\n00c53025',
    description: 'Basic arithmetic operations: add, sub, and, or'
  },
  'Memory Operations': {
    code: '8c010000\n8c020004\n00221820\nac030008',
    description: 'Load two values, add them, store result'
  },
  'Branch Test': {
    code: '00221820\n10400002\n00832022\n00a42824',
    description: 'Conditional branch with data dependency'
  },
  'Data Hazard Demo': {
    code: '00221820\n00622020\n00832820\n00a33020',
    description: 'RAW hazards demonstrating forwarding'
  }
};

// MIPS register names
const REGISTER_NAMES = [
  '$zero', '$at', '$v0', '$v1', '$a0', '$a1', '$a2', '$a3',
  '$t0', '$t1', '$t2', '$t3', '$t4', '$t5', '$t6', '$t7',
  '$s0', '$s1', '$s2', '$s3', '$s4', '$s5', '$s6', '$s7',
  '$t8', '$t9', '$k0', '$k1', '$gp', '$sp', '$fp', '$ra'
];

export default function MIPSSimulator() {
  const [cpuState, setCpuState] = useState(null);
  const [customCode, setCustomCode] = useState('');
  const [selectedProgram, setSelectedProgram] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [apiPort, setApiPort] = useState(85);

  // API Functions
  const fetchState = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/api/state`);
      if (!res.ok) throw new Error('Failed to fetch state');
      const data = await res.json();
      setCpuState(data);
      return true;
    } catch (err) {
      setMessage({ type: 'error', text: `Cannot connect to server at ${API_BASE}. Make sure the backend is running.` });
      return false;
    }
  }, []);

  const loadProgram = async (code) => {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/load?start=0`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: code
      });
      
      if (!res.ok) {
        const error = await res.json();
        throw new Error(error.error || 'Failed to load program');
      }
      
      const result = await res.json();
      await fetchState();
      setMessage({ 
        type: 'success', 
        text: `✓ Program loaded: ${result.loaded} instruction(s) at address ${result.start}` 
      });
      setTimeout(() => setMessage(null), 3000);
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to load program' });
    } finally {
      setIsLoading(false);
    }
  };

  const step = async (cycles = 1) => {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/step?cycles=${cycles}`, { method: 'POST' });
      if (!res.ok) throw new Error('Step failed');
      await fetchState();
      setMessage({ type: 'success', text: `✓ Executed ${cycles} cycle(s)` });
      setTimeout(() => setMessage(null), 2000);
    } catch (err) {
      setMessage({ type: 'error', text: 'Execution failed' });
    } finally {
      setIsLoading(false);
    }
  };

  const reset = async () => {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/reset?clearRegs=1&clearMem=1&pc=0`, { method: 'POST' });
      if (!res.ok) throw new Error('Reset failed');
      await fetchState();
      setCustomCode('');
      setSelectedProgram('');
      setMessage({ type: 'success', text: '✓ Simulator reset' });
      setTimeout(() => setMessage(null), 2000);
    } catch (err) {
      setMessage({ type: 'error', text: 'Reset failed' });
    } finally {
      setIsLoading(false);
    }
  };

  // Effects
  useEffect(() => {
    fetchState();
    const interval = setInterval(fetchState, 2000); // Auto-refresh every 2 seconds
    return () => clearInterval(interval);
  }, [fetchState]);

  useEffect(() => {
    if (isRunning) {
      const interval = setInterval(() => step(1), 500);
      return () => clearInterval(interval);
    }
  }, [isRunning]);


  const handleLoadSample = (programName) => {
    setSelectedProgram(programName);
    const program = SAMPLE_PROGRAMS[programName];
    setCustomCode(program.code);
    loadProgram(program.code);
  };

  const handleLoadCustom = () => {
    if (!customCode.trim()) {
      setMessage({ type: 'error', text: 'Please enter some code first' });
      return;
    }
    setSelectedProgram('');
    loadProgram(customCode);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 text-white">
      <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
        
        {/* Header */}
        <header className="text-center mb-8">
          <div className="flex items-center justify-center gap-3 mb-2">
            <Cpu className="w-10 h-10 sm:w-12 sm:h-12 text-purple-400" />
            <h1 className="text-3xl sm:text-4xl lg:text-5xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
              MIPS Pipeline Simulator
            </h1>
          </div>
          <p className="text-slate-300 text-sm sm:text-base">5-Stage Pipelined Processor Visualization</p>
        </header>

        {/* Status Message */}
        {message && (
          <div className={`mb-6 p-4 rounded-lg border-2 flex items-center gap-2 animate-fade-in ${
            message.type === 'error' 
              ? 'bg-red-500/20 border-red-500' 
              : 'bg-green-500/20 border-green-500'
          }`}>
            {message.type === 'error' ? <AlertCircle className="w-5 h-5" /> : <CheckCircle className="w-5 h-5" />}
            <span>{message.text}</span>
          </div>
        )}

        {/* Main Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          
          {/* Left Column: Controls */}
          <div className="lg:col-span-3 space-y-6">
            
            {/* Sample Programs */}
            <Card title="📚 Sample Programs">
              <div className="space-y-2">
                {Object.keys(SAMPLE_PROGRAMS).map((name) => (
                  <button
                    key={name}
                    onClick={() => handleLoadSample(name)}
                    disabled={isLoading}
                    className={`w-full text-left p-3 rounded-lg transition-all ${
                      selectedProgram === name
                        ? 'bg-purple-600 border-2 border-purple-400'
                        : 'bg-slate-800 hover:bg-slate-700 border-2 border-transparent'
                    } ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
                  >
                    <div className="font-semibold">{name}</div>
                    <div className="text-xs text-slate-400 mt-1">
                      {SAMPLE_PROGRAMS[name].description}
                    </div>
                  </button>
                ))}
              </div>
            </Card>

            {/* Custom Program */}
            <Card title="✏️ Custom Program">
              <textarea
                value={customCode}
                onChange={(e) => setCustomCode(e.target.value)}
                placeholder="Enter hex instructions (one per line)&#10;Example:&#10;00221820&#10;00832022"
                className="w-full h-32 bg-slate-900 border-2 border-slate-700 rounded-lg p-3 font-mono text-sm focus:outline-none focus:border-purple-500 resize-none"
                disabled={isLoading}
              />
              <button
                onClick={handleLoadCustom}
                disabled={isLoading}
                className="w-full mt-3 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-500 hover:to-pink-500 px-4 py-2 rounded-lg font-semibold transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Upload className="w-4 h-4" />
                {isLoading ? 'Loading...' : 'Load Custom Code'}
              </button>
            </Card>

            {/* Execution Controls */}
            <Card title="⚡ Execution Control">
              <div className="space-y-2">
                <button
                  onClick={() => step(1)}
                  disabled={isLoading}
                  className="w-full bg-blue-600 hover:bg-blue-500 px-4 py-3 rounded-lg font-semibold transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <StepForward className="w-4 h-4" />
                  Step 1 Cycle
                </button>
                <button
                  onClick={() => step(5)}
                  disabled={isLoading}
                  className="w-full bg-blue-600 hover:bg-blue-500 px-4 py-3 rounded-lg font-semibold transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <StepForward className="w-4 h-4" />
                  Step 5 Cycles
                </button>
                <button
                  onClick={() => setIsRunning(!isRunning)}
                  disabled={isLoading}
                  className={`w-full px-4 py-3 rounded-lg font-semibold transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed ${
                    isRunning 
                      ? 'bg-red-600 hover:bg-red-500' 
                      : 'bg-green-600 hover:bg-green-500'
                  }`}
                >
                  {isRunning ? <><Pause className="w-5 h-5" />Pause</> : <><Play className="w-5 h-5" />Run Auto</>}
                </button>
                <button
                  onClick={reset}
                  disabled={isLoading}
                  className="w-full bg-slate-600 hover:bg-slate-500 px-4 py-3 rounded-lg font-semibold transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <RotateCcw className="w-5 h-5" />
                  Reset All
                </button>
                <button
                  onClick={fetchState}
                  disabled={isLoading}
                  className="w-full bg-slate-700 hover:bg-slate-600 px-4 py-3 rounded-lg font-semibold transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <RefreshCw className="w-4 h-4" />
                  Refresh State
                </button>
              </div>
            </Card>

            {/* Program Counter */}
            {cpuState && (
              <Card title="📍 Program Counter">
                <div className="bg-slate-900 rounded-lg p-6 text-center">
                  <div className="text-4xl font-mono font-bold text-purple-400">
                    {cpuState.pc || 0}
                  </div>
                  <div className="text-sm text-slate-400 mt-2">
                    0x{(cpuState.pc || 0).toString(16).toUpperCase().padStart(8, '0')}
                  </div>
                </div>
              </Card>
            )}
          </div>

          {/* Middle Column: Pipeline */}
          <div className="lg:col-span-5">
            <Card title="🔄 Pipeline Stages">
              {cpuState?.pipeline ? (
                <PipelineVisualization pipeline={cpuState.pipeline} />
              ) : (
                <EmptyState text="No pipeline data available" />
              )}
            </Card>
          </div>

          {/* Right Column: Registers & Memory */}
          <div className="lg:col-span-4 space-y-6">
            <Card title="💾 Registers">
              {cpuState?.registers ? (
                <RegisterGrid registers={cpuState.registers} />
              ) : (
                <EmptyState text="No register data available" />
              )}
            </Card>

            {cpuState?.dataMemory && Object.keys(cpuState.dataMemory).length > 0 && (
              <Card title="📦 Data Memory">
                <DataMemoryView memory={cpuState.dataMemory} />
              </Card>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

// Reusable Components
function Card({ title, children }) {
  return (
    <div className="bg-slate-800/50 backdrop-blur rounded-xl p-6 border border-slate-700 shadow-lg">
      <h2 className="text-xl font-bold mb-4 text-purple-300">{title}</h2>
      {children}
    </div>
  );
}

function PipelineVisualization({ pipeline }) {
  const stages = [
    { name: 'IF', key: 'IF_ID', label: 'Instruction Fetch' },
    { name: 'ID', key: 'ID_EX', label: 'Instruction Decode' },
    { name: 'EX', key: 'EX_MEM', label: 'Execute' },
    { name: 'MEM', key: 'EX_MEM', label: 'Memory Access', isMEM: true },
    { name: 'WB', key: 'MEM_WB', label: 'Write Back', isWB: true }
  ];

  return (
    <div className="space-y-4">
      {stages.map((stage, index) => {
        const stageData = pipeline[stage.key];
        const hasInstruction = stageData?.instruction && 
          stageData.instruction !== null && 
          stageData.instruction.hex && 
          stageData.instruction.hex !== 'null';
        
        return (
          <div key={stage.name} className="relative">
            {/* Stage Box */}
            <div className={`bg-slate-900 rounded-lg p-4 border-2 transition-all ${
              hasInstruction 
                ? 'border-purple-500 shadow-lg shadow-purple-500/20' 
                : 'border-slate-700'
            }`}>
              <div className="flex justify-between items-center mb-2">
                <div>
                  <span className="font-bold text-lg text-purple-400">{stage.name}</span>
                  <span className="text-xs text-slate-400 ml-2">{stage.label}</span>
                </div>
                <span className={`px-2 py-1 rounded text-xs font-semibold ${
                  hasInstruction ? 'bg-green-500' : 'bg-slate-700'
                }`}>
                  {hasInstruction ? 'ACTIVE' : 'EMPTY'}
                </span>
              </div>
              
              {hasInstruction ? (
                <div className="space-y-1">
                  <div className="font-mono text-sm text-slate-300">
                    {stageData.instruction.hex}
                  </div>
                  {stageData.pc !== undefined && stageData.pc !== null && (
                    <div className="text-xs text-slate-400">PC: {stageData.pc}</div>
                  )}
                  {stageData.aluResult !== undefined && stageData.aluResult !== null && (
                    <div className="text-xs text-slate-400">ALU: {stageData.aluResult}</div>
                  )}
                  {stageData.destReg !== undefined && stageData.destReg !== null && stageData.destReg >= 0 && (
                    <div className="text-xs text-purple-300">Dest: {REGISTER_NAMES[stageData.destReg] || `$${stageData.destReg}`}</div>
                  )}
                  {stageData.branchTaken !== undefined && stageData.branchTaken !== null && (
                    <div className={`text-xs ${stageData.branchTaken ? 'text-red-400' : 'text-slate-400'}`}>
                      Branch: {stageData.branchTaken ? 'TAKEN' : 'NOT TAKEN'}
                    </div>
                  )}
                  {stage.isWB && stageData.writeData !== undefined && stageData.writeData !== null && (
                    <div className="text-xs text-green-300">Write: {stageData.writeData} → {REGISTER_NAMES[stageData.destReg] || `$${stageData.destReg}`}</div>
                  )}
                  {stage.isMEM && stageData.memRead && (
                    <div className="text-xs text-blue-300">Load from memory</div>
                  )}
                  {stage.isMEM && stageData.memWrite && (
                    <div className="text-xs text-orange-300">Store to memory</div>
                  )}
                </div>
              ) : (
                <div className="text-sm text-slate-500 italic">No instruction</div>
              )}
            </div>
            
            {/* Arrow to next stage */}
            {index < stages.length - 1 && (
              <div className="flex justify-center my-2">
                <div className="w-0.5 h-4 bg-purple-500"></div>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}

function RegisterGrid({ registers }) {
  // Handle both array and object formats
  const registerArray = Array.isArray(registers) 
    ? registers 
    : Object.values(registers).map((v, i) => ({ index: i, value: v }));
  
  return (
    <div className="grid grid-cols-2 gap-2 max-h-[600px] overflow-y-auto custom-scrollbar">
      {Array.isArray(registers) ? (
        registers.map((value, index) => (
          <RegisterDisplay 
            key={index} 
            name={REGISTER_NAMES[index]} 
            value={value}
            index={index}
          />
        ))
      ) : (
        Object.entries(registers).map(([key, value]) => {
          const index = key.startsWith('$') 
            ? REGISTER_NAMES.indexOf(key)
            : parseInt(key.replace('$', '')) || parseInt(key);
          return (
            <RegisterDisplay 
              key={key} 
              name={REGISTER_NAMES[index] || key} 
              value={value}
              index={index}
            />
          );
        })
      )}
    </div>
  );
}

function RegisterDisplay({ name, value, index }) {
  const isZero = index === 0;
  const hasValue = value !== 0 || isZero;
  
  return (
    <div className={`bg-slate-900 rounded p-2 border transition-all ${
      hasValue ? 'border-purple-500/50' : 'border-slate-700'
    }`}>
      <div className="text-xs text-purple-400 font-bold">{name}</div>
      <div className="font-mono text-sm text-slate-300">
        {value}
      </div>
      <div className="text-xs text-slate-500 font-mono">
        0x{value.toString(16).toUpperCase().padStart(8, '0')}
      </div>
    </div>
  );
}

function DataMemoryView({ memory }) {
  const entries = Object.entries(memory)
    .map(([addr, value]) => ({ addr: parseInt(addr), value }))
    .sort((a, b) => a.addr - b.addr)
    .slice(0, 20); // Show first 20 non-zero entries

  if (entries.length === 0) {
    return <div className="text-slate-400 text-sm">No data in memory</div>;
  }

  return (
    <div className="space-y-1 max-h-[300px] overflow-y-auto custom-scrollbar">
      {entries.map(({ addr, value }) => (
        <div key={addr} className="flex justify-between items-center p-2 bg-slate-900 rounded text-sm">
          <span className="font-mono text-purple-400">0x{addr.toString(16).toUpperCase().padStart(8, '0')}</span>
          <span className="font-mono text-slate-300">{value}</span>
        </div>
      ))}
    </div>
  );
}

function EmptyState({ text }) {
  return (
    <div className="text-center text-slate-400 py-8">
      {text}
    </div>
  );
}
