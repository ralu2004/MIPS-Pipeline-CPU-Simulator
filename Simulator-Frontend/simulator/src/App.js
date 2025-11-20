import React, { useState, useEffect, useCallback } from 'react';
import { Play, Pause, RotateCcw, Upload, Cpu, AlertCircle, CheckCircle, StepForward, RefreshCw, ChevronDown, ChevronUp } from 'lucide-react';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:85';

const REGISTER_NAMES = [
  '$zero', '$at', '$v0', '$v1', '$a0', '$a1', '$a2', '$a3',
  '$t0', '$t1', '$t2', '$t3', '$t4', '$t5', '$t6', '$t7',
  '$s0', '$s1', '$s2', '$s3', '$s4', '$s5', '$s6', '$s7',
  '$t8', '$t9', '$k0', '$k1', '$gp', '$sp', '$fp', '$ra'
];

const SAMPLE_PROGRAMS = {
  'Simple ADD': {
    code: '00221820\n00832022\n00a42824\n00c53025',
    assembly: 'add $3, $1, $2\nadd $4, $4, $3\nand $5, $5, $4\nor $6, $6, $5',
    description: 'Basic arithmetic operations'
  },
  'Memory Operations': {
    code: '8c010000\n8c020004\n00221820\nac030008',
    assembly: 'lw $1, 0($0)\nlw $2, 4($0)\nadd $3, $1, $2\nsw $3, 8($0)',
    description: 'Load, compute, and store'
  },
  'Branch Test': {
    code: '00221820\n10400002\n00832022\n00a42824',
    assembly: 'add $3, $1, $2\nbeq $2, $0, 2\nadd $4, $4, $3\nand $5, $5, $4',
    description: 'Conditional branching'
  },
  'Data Hazard Demo': {
    code: '00221820\n00622020\n00832820\n00a33020',
    assembly: 'add $3, $1, $2\nadd $4, $3, $2\nadd $5, $4, $3\nadd $6, $5, $3',
    description: 'Back-to-back dependencies'
  }
};

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

const STAGE_INFO = {
  'IF': 'Instruction Fetch: Fetches the next instruction from memory using the Program Counter',
  'ID': 'Instruction Decode: Decodes the instruction and reads register operands',
  'EX': 'Execute: Performs ALU operations or calculates memory addresses',
  'MEM': 'Memory Access: Reads from or writes to data memory for load/store instructions',
  'WB': 'Write Back: Writes the result back to the destination register'
};

export default function MIPSSimulator() {
  const [cpuState, setCpuState] = useState(null);
  const [customCode, setCustomCode] = useState('');
  const [selectedProgram, setSelectedProgram] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showProgramLoader, setShowProgramLoader] = useState(true);
  const [pipelineView, setPipelineView] = useState('detailed'); 
  const [executionHistory, setExecutionHistory] = useState([]);


  const fetchState = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/api/state`);
      if (!res.ok) throw new Error('Failed to fetch state');
      const data = await res.json();
      setCpuState(data);
      
      if (data.pipeline) {
        setExecutionHistory(prev => [...prev, {
          cycle: prev.length,
          pipeline: data.pipeline
        }]);
      }
      
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
      setExecutionHistory([]); 
      await fetchState();
      setMessage({ 
        type: 'success', 
        text: `✓ Program loaded: ${result.loaded} instruction(s)` 
      });
      setTimeout(() => setMessage(null), 3000);
      setShowProgramLoader(false); 
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
      setTimeout(() => setMessage(null), 1500);
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
      
      setCpuState(null);
      setExecutionHistory([]);
      
      await fetchState();
      
      setCustomCode('');
      setSelectedProgram('');
      setShowProgramLoader(true);
      setMessage({ type: 'success', text: '✓ Simulator reset' });
      setTimeout(() => setMessage(null), 2000);
    } catch (err) {
      setMessage({ type: 'error', text: 'Reset failed' });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {}, [fetchState]);

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
      
      {/* FIXED CONTROL BAR */}
      <div className="sticky top-0 z-50 bg-slate-900/95 backdrop-blur-lg border-b-2 border-purple-500/30 shadow-xl">
        <div className="max-w-7xl mx-auto px-4 py-4">
          {/* Header */}
          <div className="flex flex-col sm:flex-row items-center justify-between mb-4 gap-4">
            <div className="flex items-center gap-3">
              <Cpu className="w-8 h-8 text-purple-400" />
              <div>
                <h1 className="text-2xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
                  MIPS Pipeline Simulator
                </h1>
                <p className="text-xs text-slate-400">5-Stage Pipelined Processor</p>
              </div>
            </div>

            {/* Program Counter Display */}
            {cpuState && (
              <div className="flex items-center gap-4 bg-slate-800/80 rounded-lg px-4 py-2 border border-purple-500/30">
                <div className="text-center">
                  <div className="text-xs text-slate-400">PC</div>
                  <div className="text-2xl font-mono font-bold text-purple-400">
                    {cpuState.pc || 0}
                  </div>
                </div>
                <div className="text-xs text-slate-400">
                  0x{(cpuState.pc || 0).toString(16).toUpperCase().padStart(8, '0')}
                </div>
              </div>
            )}
          </div>

          {/* Control Buttons */}
          <div className="flex flex-wrap items-center gap-2">
            <button
              onClick={() => step(1)}
              disabled={isLoading}
              className="bg-blue-600 hover:bg-blue-500 px-4 py-2 rounded-lg font-semibold transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              <StepForward className="w-4 h-4" />
              Step 1
            </button>
            <button
              onClick={() => step(5)}
              disabled={isLoading}
              className="bg-blue-600 hover:bg-blue-500 px-4 py-2 rounded-lg font-semibold transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              <StepForward className="w-4 h-4" />
              Step 5
            </button>
            <button
              onClick={() => setIsRunning(!isRunning)}
              disabled={isLoading}
              className={`px-4 py-2 rounded-lg font-semibold transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed text-sm ${
                isRunning 
                  ? 'bg-red-600 hover:bg-red-500' 
                  : 'bg-green-600 hover:bg-green-500'
              }`}
            >
              {isRunning ? <><Pause className="w-4 h-4" />Pause</> : <><Play className="w-4 h-4" />Run</>}
            </button>
            <button
              onClick={reset}
              disabled={isLoading}
              className="bg-slate-600 hover:bg-slate-500 px-4 py-2 rounded-lg font-semibold transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              <RotateCcw className="w-4 h-4" />
              Reset
            </button>
            <button
              onClick={fetchState}
              disabled={isLoading}
              className="bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg font-semibold transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              <RefreshCw className="w-4 h-4" />
              Refresh
            </button>
            <button
              onClick={() => setShowProgramLoader(!showProgramLoader)}
              className="ml-auto bg-purple-600 hover:bg-purple-500 px-4 py-2 rounded-lg font-semibold transition-all flex items-center gap-2 text-sm"
            >
              <Upload className="w-4 h-4" />
              Load Program
              {showProgramLoader ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
        
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

        {/* COLLAPSIBLE PROGRAM LOADER */}
        {showProgramLoader && (
          <div className="mb-6 animate-fade-in">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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
                      <div className="font-semibold mb-1">{name}</div>
                      <div className="text-xs text-slate-400 mb-2">
                        {SAMPLE_PROGRAMS[name].description}
                      </div>
                      <div className="font-mono text-xs bg-slate-900/50 p-2 rounded border border-slate-700">
                        {SAMPLE_PROGRAMS[name].assembly.split('\n').map((line, i) => (
                          <div key={i} className="text-green-400">{line}</div>
                        ))}
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
                  className="w-full h-40 bg-slate-900 border-2 border-slate-700 rounded-lg p-3 font-mono text-sm focus:outline-none focus:border-purple-500 resize-none"
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
            </div>
          </div>
        )}

        {/* SIMULATION VIEW */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          
          {/* Pipeline Stages - Takes up more space */}
          <div className="lg:col-span-6">
            <Card title="🔄 Pipeline Stages">
              {/* View Toggle */}
              <div className="flex gap-2 mb-4">
                <button
                  onClick={() => setPipelineView('detailed')}
                  className={`flex-1 px-4 py-2 rounded-lg font-semibold transition-all text-sm ${
                    pipelineView === 'detailed'
                      ? 'bg-purple-600 text-white'
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  }`}
                >
                  📋 Detailed
                </button>
                <button
                  onClick={() => setPipelineView('diagram')}
                  className={`flex-1 px-4 py-2 rounded-lg font-semibold transition-all text-sm ${
                    pipelineView === 'diagram'
                      ? 'bg-purple-600 text-white'
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  }`}
                >
                  📊 Diagram
                </button>
                <button
                  onClick={() => setPipelineView('gantt')}
                  className={`flex-1 px-4 py-2 rounded-lg font-semibold transition-all text-sm ${
                    pipelineView === 'gantt'
                      ? 'bg-purple-600 text-white'
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  }`}
                >
                  📈 Timeline
                </button>
              </div>

              {cpuState?.pipeline ? (
                pipelineView === 'detailed' ? (
                  <PipelineVisualization pipeline={cpuState.pipeline} />
                ) : pipelineView === 'diagram' ? (
                  <PipelineDiagram pipeline={cpuState.pipeline} />
                ) : (
                  <PipelineGantt history={executionHistory} />
                )
              ) : (
                <EmptyState text="No pipeline data available" />
              )}
            </Card>
          </div>

          {/* Registers and Memory */}
          <div className="lg:col-span-6 space-y-6">
            {/* Registers */}
            <Card title="💾 Registers">
              {cpuState?.registers ? (
                <RegisterGrid registers={cpuState.registers} />
              ) : (
                <EmptyState text="No register data available" />
              )}
            </Card>

            {/* Data Memory */}
            <Card title="📦 Data Memory">
              {cpuState?.dataMemory ? (
                <MemoryGrid memory={cpuState.dataMemory} />
              ) : (
                <EmptyState text="Memory view will appear here when available" />
              )}
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}

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
    { name: 'MEM', key: 'MEM_WB', label: 'Memory Access' },
    { name: 'WB', key: 'MEM_WB', label: 'Write Back' }
  ];

  return (
    <div className="space-y-3">
      {stages.map((stage, index) => {
        const stageData = pipeline[stage.key];
        const hasInstruction = stageData?.instruction && 
          stageData.instruction !== null && 
          stageData.instruction.hex && 
          stageData.instruction.hex !== 'null';
        
        return (
          <div key={stage.name} className="relative group">
            <div className={`bg-slate-900 rounded-lg p-4 border-2 transition-all ${
              hasInstruction 
                ? 'border-purple-500 shadow-lg shadow-purple-500/20' 
                : 'border-slate-700'
            }`}>
              <div className="flex justify-between items-center mb-2">
                <div className="relative">
                  <span className="font-bold text-lg text-purple-400">{stage.name}</span>
                  <span className="text-xs text-slate-400 ml-2">{stage.label}</span>
                  {/* Tooltip on hover */}
                  <div className="absolute left-0 top-8 w-72 bg-slate-800 border border-purple-500 rounded-lg p-3 text-xs text-slate-300 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-10 shadow-xl">
                    <div className="font-bold text-purple-400 mb-1">{stage.name} Stage</div>
                    {STAGE_INFO[stage.name]}
                  </div>
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
                  {stageData.pc !== undefined && (
                    <div className="text-xs text-slate-400">PC: {stageData.pc}</div>
                  )}
                  {stageData.destReg !== undefined && stageData.destReg >= 0 && (
                    <div className="text-xs text-purple-300">
                      Dest: {REGISTER_NAMES[stageData.destReg] || `${stageData.destReg}`}
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-sm text-slate-500 italic">No instruction</div>
              )}
            </div>
            
            {index < stages.length - 1 && (
              <div className="flex justify-center my-1">
                <div className="w-0.5 h-3 bg-purple-500/50"></div>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}

function RegisterGrid({ registers }) {
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
}

function MemoryGrid({ memory }) {
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

function EmptyState({ text }) {
  return (
    <div className="text-center text-slate-400 py-8 text-sm">
      {text}
    </div>
  );
}

function PipelineGantt({ history }) {
  if (!history || history.length === 0) {
    return (
      <div className="bg-slate-900 rounded-lg p-6 text-center text-slate-400 text-sm">
        Timeline will appear as you step through execution
      </div>
    );
  }

  const stages = ['IF', 'ID', 'EX', 'MEM', 'WB'];
  const stageKeys = ['IF_ID', 'ID_EX', 'EX_MEM', 'EX_MEM', 'MEM_WB'];

  return (
    <div className="bg-slate-900 rounded-lg p-4 overflow-x-auto">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-bold text-purple-300">📊 Pipeline Gantt Chart</h3>
        <span className="text-xs text-slate-400">{history.length} cycles</span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full border-collapse text-xs">
          <thead>
            <tr className="bg-slate-800">
              <th className="border border-slate-700 p-2 text-left text-purple-400 font-bold">Cycle</th>
              {stages.map(stage => (
                <th key={stage} className="border border-slate-700 p-2 text-center font-bold text-purple-400">
                  {stage}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {history.map((snapshot, cycleIdx) => (
              <tr key={cycleIdx} className="hover:bg-slate-800/50">
                <td className="border border-slate-700 p-2 font-mono font-bold text-white bg-slate-800">
                  {snapshot.cycle}
                </td>
                {stages.map((stage, stageIdx) => {
                  const stageKey = stageKeys[stageIdx];
                  const stageData = snapshot.pipeline[stageKey];
                  
                  const hasInstruction = stageData?.instruction?.hex && 
                    stageData.instruction.hex !== 'null' &&
                    stageData.instruction.hex !== '0x00000000';

                  let cellClass = 'border border-slate-700 p-2 text-center font-mono';
                  let cellContent = '';
                  let bgColor = '';

                  if (!hasInstruction) {
                    // BUBBLE
                    cellClass += ' text-purple-300';
                    cellContent = 'BUBBLE';
                    bgColor = '#7c3aed';
                  } else if (stageData.branchTaken) {
                    // FLUSH
                    cellClass += ' text-white font-bold';
                    cellContent = 'FLUSH';
                    bgColor = '#ef4444'; 
                  } else {
                    // INSTRUCTION
                    cellClass += ' text-white';
                    cellContent = stageData.instruction.assembly?.split(' ')[0] || 'INSTR';
                    bgColor = '#3b82f6'; 
                  }

                  return (
                    <td
                      key={stage}
                      className={cellClass}
                      style={{ backgroundColor: bgColor }}
                      title={hasInstruction ? stageData.instruction.assembly : 'Bubble'}
                    >
                      {cellContent}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Legend */}
      <div className="mt-4 flex flex-wrap gap-4 text-xs">
        <div className="flex items-center gap-2">
          <div className="w-6 h-4 rounded" style={{ backgroundColor: '#3b82f6' }}></div>
          <span className="text-slate-300">Instruction</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-4 rounded" style={{ backgroundColor: '#7c3aed' }}></div>
          <span className="text-slate-300">Bubble</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-4 rounded" style={{ backgroundColor: '#ef4444' }}></div>
          <span className="text-slate-300">Flush (Branch)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-4 rounded" style={{ backgroundColor: '#f59e0b' }}></div>
          <span className="text-slate-300">Stall (if detected)</span>
        </div>
      </div>
    </div>
  );
}

function PipelineDiagram({ pipeline }) {
  const stages = [
    { name: 'IF', key: 'IF_ID', color: 'bg-blue-500' },
    { name: 'ID', key: 'ID_EX', color: 'bg-green-500' },
    { name: 'EX', key: 'EX_MEM', color: 'bg-yellow-500' },
    { name: 'MEM', key: 'EX_MEM', color: 'bg-orange-500' },
    { name: 'WB', key: 'MEM_WB', color: 'bg-purple-500' }
  ];

  return (
    <div className="bg-slate-900 rounded-lg p-6">
      {/* Horizontal Pipeline Stages */}
      <div className="flex items-center justify-between mb-8">
        {stages.map((stage, index) => {
          const stageData = pipeline[stage.key];
          const hasInstruction = stageData?.instruction?.hex && 
            stageData.instruction.hex !== 'null' &&
            stageData.instruction.hex !== '0x00000000';
          
          return (
            <React.Fragment key={stage.name}>
              {/* Stage Box */}
              <div className="relative group flex-1">
                <div className={`${stage.color} ${hasInstruction ? 'opacity-100' : 'opacity-30'} rounded-lg p-4 text-center transition-all`}>
                  <div className="font-bold text-white text-xl mb-2">{stage.name}</div>
                  {hasInstruction ? (
                    <div className="text-xs text-white/90 font-mono">
                      {stageData.instruction.hex?.substring(0, 10)}...
                    </div>
                  ) : (
                    <div className="text-xs text-white/60">Empty</div>
                  )}
                </div>
                
                {/* Tooltip with details */}
                {hasInstruction && (
                  <div className="absolute top-full left-1/2 transform -translate-x-1/2 mt-2 w-64 bg-slate-800 border-2 border-purple-500 rounded-lg p-3 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-20 shadow-2xl">
                    <div className="text-xs space-y-1">
                      <div className="font-bold text-purple-400">Instruction:</div>
                      <div className="font-mono text-white">{stageData.instruction.hex}</div>
                      {stageData.instruction.assembly && (
                        <>
                          <div className="font-bold text-green-400 mt-2">Assembly:</div>
                          <div className="font-mono text-white">{stageData.instruction.assembly}</div>
                        </>
                      )}
                      {stageData.pc !== undefined && (
                        <div className="text-slate-300">PC: {stageData.pc}</div>
                      )}
                    </div>
                  </div>
                )}
              </div>
              
              {/* Arrow between stages */}
              {index < stages.length - 1 && (
                <div className="flex items-center justify-center px-2">
                  <div className="text-purple-400 text-2xl">→</div>
                </div>
              )}
            </React.Fragment>
          );
        })}
      </div>

      {/* Data Flow Visualization */}
      <div className="space-y-4 mt-8">
        <div className="text-sm font-semibold text-purple-300 mb-2">Data Path:</div>
        
        {/* Register File */}
        <div className="flex items-center gap-4">
          <div className="bg-pink-900/30 border border-pink-500 rounded p-3 flex-1">
            <div className="text-xs font-bold text-pink-400 mb-1">Register File</div>
            <div className="text-xs text-slate-300">
              {pipeline.ID_EX?.rs !== undefined && (
                <span>Reading: {REGISTER_NAMES[pipeline.ID_EX.rs]}, {REGISTER_NAMES[pipeline.ID_EX.rt]}</span>
              )}
              {pipeline.MEM_WB?.regWrite && pipeline.MEM_WB?.destReg >= 0 && (
                <span className="text-green-400 ml-2">
                  Writing: {REGISTER_NAMES[pipeline.MEM_WB.destReg]}
                </span>
              )}
            </div>
          </div>
          
          {/* ALU */}
          <div className="bg-yellow-900/30 border border-yellow-500 rounded p-3 flex-1">
            <div className="text-xs font-bold text-yellow-400 mb-1">ALU</div>
            <div className="text-xs text-slate-300">
              {pipeline.EX_MEM?.aluResult !== undefined ? (
                <span>Result: {pipeline.EX_MEM.aluResult}</span>
              ) : (
                <span className="text-slate-500">Idle</span>
              )}
            </div>
          </div>
          
          {/* Memory */}
          <div className="bg-orange-900/30 border border-orange-500 rounded p-3 flex-1">
            <div className="text-xs font-bold text-orange-400 mb-1">Data Memory</div>
            <div className="text-xs text-slate-300">
              {pipeline.EX_MEM?.memRead && <span className="text-blue-400">Reading...</span>}
              {pipeline.EX_MEM?.memWrite && <span className="text-red-400">Writing...</span>}
              {!pipeline.EX_MEM?.memRead && !pipeline.EX_MEM?.memWrite && (
                <span className="text-slate-500">Idle</span>
              )}
            </div>
          </div>
        </div>

        {/* Control Signals */}
        <div className="bg-slate-800/50 border border-slate-600 rounded p-3">
          <div className="text-xs font-bold text-purple-400 mb-2">Active Control Signals:</div>
          <div className="flex flex-wrap gap-2">
            {pipeline.ID_EX?.regWrite && (
              <span className="px-2 py-1 bg-green-600 rounded text-xs">RegWrite</span>
            )}
            {pipeline.EX_MEM?.memRead && (
              <span className="px-2 py-1 bg-blue-600 rounded text-xs">MemRead</span>
            )}
            {pipeline.EX_MEM?.memWrite && (
              <span className="px-2 py-1 bg-red-600 rounded text-xs">MemWrite</span>
            )}
            {pipeline.EX_MEM?.branch && (
              <span className="px-2 py-1 bg-yellow-600 rounded text-xs">Branch</span>
            )}
            {pipeline.EX_MEM?.branchTaken && (
              <span className="px-2 py-1 bg-orange-600 rounded text-xs animate-pulse">Branch Taken!</span>
            )}
            {pipeline.MEM_WB?.memToReg && (
              <span className="px-2 py-1 bg-purple-600 rounded text-xs">MemToReg</span>
            )}
            {!pipeline.ID_EX?.regWrite && !pipeline.EX_MEM?.memRead && !pipeline.EX_MEM?.memWrite && (
              <span className="px-2 py-1 bg-slate-600 rounded text-xs text-slate-400">No active signals</span>
            )}
          </div>
        </div>

        {/* Legend */}
        <div className="bg-slate-800/30 rounded p-3 mt-4">
          <div className="text-xs font-bold text-slate-400 mb-2">Color Legend:</div>
          <div className="grid grid-cols-5 gap-2 text-xs">
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-blue-500 rounded"></div>
              <span>IF</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-green-500 rounded"></div>
              <span>ID</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-yellow-500 rounded"></div>
              <span>EX</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-orange-500 rounded"></div>
              <span>MEM</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="w-3 h-3 bg-purple-500 rounded"></div>
              <span>WB</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}