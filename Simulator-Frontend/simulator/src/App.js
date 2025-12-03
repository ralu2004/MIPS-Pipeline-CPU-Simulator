import React, { useState, useEffect, useCallback } from 'react';
import { Play, Pause, RotateCcw, Upload, Cpu, AlertCircle, CheckCircle, StepForward, ChevronDown, ChevronUp } from 'lucide-react';

import PipelineGantt from './components/PipelineGantt';
import PipelineDiagram from './components/PipelineDiagram';
import PipelineVisualization from './components/PipelineVisualisation';
import RegisterGrid from './components/RegisterGrid';
import MemoryGrid from './components/MemoryGrid';
import EmptyState from './components/EmptyState';
import Card from './components/Card';
import {SAMPLE_PROGRAMS} from './components/Constants';

const API_BASE = process.env.REACT_APP_API_BASE || 'http://localhost:85';

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
      
      if (data.pipelineHistory) { 
        setExecutionHistory(data.pipelineHistory);
      } else {
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
    await reset();

    const cleanCode = code
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)   
    .join('\n');

    const res = await fetch(`${API_BASE}/api/load?start=0`, {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: cleanCode
    });
    
    if (!res.ok) {
      const error = await res.json();
      throw new Error(error.error || 'Failed to load program');
    }
    
    const result = await res.json();
    await fetchState();
    
    setMessage({ 
      type: 'success', 
      text: `Program loaded: ${result.loaded} instruction(s)` 
    });
    setTimeout(() => setMessage(null), 3000);
    setShowProgramLoader(false); 
  } catch (err) {
    setMessage({ type: 'error', text: err.message || 'Failed to load program' });
  } finally {
    setIsLoading(false);
  }
};

  const step = useCallback(async (cycles = 1) => {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/step?cycles=${cycles}`, { method: 'POST' });
      if (!res.ok) throw new Error('Step failed');
      await fetchState();
      setMessage({ type: 'success', text: `Executed ${cycles} cycle(s)` });
      setTimeout(() => setMessage(null), 1500);
    } catch (err) {
      setMessage({ type: 'error', text: 'Execution failed' });
    } finally {
      setIsLoading(false);
    }
  }, [fetchState]);

  const reset = async (showLoader = true) => {
    try {
      const res = await fetch(`${API_BASE}/api/reset?clearRegs=1&clearMem=1&pc=0`, { method: 'POST' });
      if (!res.ok) throw new Error('Reset failed');

      setCpuState(null);
      setExecutionHistory([]);
      setCustomCode('');
      setSelectedProgram('');
      if (showLoader) setShowProgramLoader(true);
      
      await new Promise(resolve => setTimeout(resolve, 50)); 
      await fetchState();
      
      if (showLoader) {
        setMessage({ type: 'success', text: 'Simulator reset' });
        setTimeout(() => setMessage(null), 2000);
      }
    } catch (err) {
      setMessage({ type: 'error', text: 'Reset failed' });
      throw err; 
    }
  };

  useEffect(() => {
    fetchState();
  }, [fetchState]);

  useEffect(() => {
    if (isRunning) {
      const interval = setInterval(() => step(1), 500);
      return () => clearInterval(interval);
    }
  }, [isRunning, step]);

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
      
      <div className="sticky top-0 z-50 bg-slate-900/95 backdrop-blur-lg border-b-2 border-purple-500/30 shadow-xl">
        <div className="max-w-7xl mx-auto px-4 py-4">
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

        {showProgramLoader && (
          <div className="mb-6 animate-fade-in">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
           
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
                        {SAMPLE_PROGRAMS[name].code.split('\n').map((line, i) => (
                          <div key={i} className="text-green-400">{line}</div>
                        ))}
                      </div>
                      <div className="font-mono text-xs bg-slate-900/50 p-2 rounded border border-slate-700">
                        {SAMPLE_PROGRAMS[name].expectedResults}
                      </div>
                      
                    </button>
                  ))}
                </div>
              </Card>

              <Card title="✏️ Custom Program">
                <textarea
                  value={customCode}
                  onChange={(e) => setCustomCode(e.target.value)}
                  placeholder="Enter MIPS assembly (one per line)&#10;Example:&#10;add $t0, $t1, $t2&#10;lw $s0, 0($sp)"
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

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <div className="lg:col-span-6">
            <Card title="🔄 Pipeline Stages">
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
                  <PipelineVisualization pipeline={cpuState.pipeline} currentSnapshot={executionHistory.length > 0 ? executionHistory[executionHistory.length - 1] : null}
                  />
                ) : pipelineView === 'diagram' ? (
                  <PipelineDiagram pipeline={cpuState.pipeline} 
                    currentSnapshot={executionHistory.length > 0 ? executionHistory[executionHistory.length - 1] : null}
                  />
                ) : (
                  <PipelineGantt history={executionHistory} />
                )
              ) : (
                <EmptyState text="No pipeline data available" />
              )}
            </Card>
          </div>

          <div className="lg:col-span-6 space-y-6">
            <Card title="💾 Registers">
              {cpuState?.registers ? (
                <RegisterGrid registers={cpuState.registers} />
              ) : (
                <EmptyState text="No register data available" />
              )}
            </Card>

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