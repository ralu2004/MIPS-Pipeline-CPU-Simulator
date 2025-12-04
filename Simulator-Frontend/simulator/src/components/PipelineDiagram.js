import { REGISTER_NAMES } from './Constants';

import React, { useState } from 'react';
import { BookOpen, Cpu, Database, Zap, GitBranch, ArrowRight, Info, X } from 'lucide-react';

export default function PipelineDiagram({ pipeline, currentSnapshot }) {
  const [selectedTopic, setSelectedTopic] = useState(null);

  const stageDataView = currentSnapshot ? {
    IF: currentSnapshot.IF ?? {},
    ID: currentSnapshot.ID ?? {},
    EX: currentSnapshot.EX ?? {},
    MEM: currentSnapshot.MEM ?? {},
    WB: currentSnapshot.WB ?? {}
  } : {
    IF: pipeline.IF_ID ?? {},
    ID: pipeline.ID_EX ?? {},
    EX: pipeline.EX_MEM ?? {},
    MEM: pipeline.MEM_WB ?? {},
    WB: {}
  };

  const hasInstruction = (stageData) => {
    return stageData?.instruction?.hex && 
           stageData.instruction.hex !== 'null' && 
           stageData.instruction.hex !== '0x00000000';
  };

  const educationalContent = {
    'overview': {
      title: '5-Stage MIPS Pipeline',
      icon: <Cpu className="text-purple-400" size={24} />,
      content: `The MIPS pipeline divides instruction execution into 5 stages, allowing multiple instructions to execute simultaneously. Each stage takes one clock cycle, increasing throughput from 1 instruction per 5 cycles to 1 instruction per cycle (ideally).`,
      color: 'purple'
    },
    'IF': {
      title: 'Instruction Fetch',
      icon: <BookOpen className="text-blue-400" size={24} />,
      content: `Fetches the instruction from instruction memory using the Program Counter (PC). The PC is incremented by 4 to point to the next instruction.`,
      signals: ['PC', 'Instruction', 'PC+4'],
      color: 'blue'
    },
    'ID': {
      title: 'Instruction Decode',
      icon: <GitBranch className="text-green-400" size={24} />,
      content: `Decodes the instruction to determine the operation. Reads source registers from the register file. Sign-extends immediate values. The control unit generates all control signals.`,
      signals: ['Control Signals', 'ReadData1', 'ReadData2', 'SignExtImm'],
      color: 'green'
    },
    'EX': {
      title: 'Execute',
      icon: <Zap className="text-yellow-400" size={24} />,
      content: `Performs ALU operations, calculates memory addresses, or evaluates branch conditions. The forwarding unit detects data hazards and provides data from later stages if needed.`,
      signals: ['ALU Result', 'Zero Flag', 'Branch Target'],
      color: 'yellow'
    },
    'MEM': {
      title: 'Memory Access',
      icon: <Database className="text-orange-400" size={24} />,
      content: `Accesses data memory for load/store instructions. For loads, reads data from memory. For stores, writes data to memory. Other instructions pass through unchanged.`,
      signals: ['Memory Data', 'Address', 'MemRead', 'MemWrite'],
      color: 'orange'
    },
    'WB': {
      title: 'Write Back',
      icon: <ArrowRight className="text-purple-400" size={24} />,
      content: `Writes the result back to the destination register. The result comes either from the ALU or from memory, selected by the MemToReg control signal.`,
      signals: ['Write Data', 'Destination Register', 'RegWrite'],
      color: 'purple'
    },
    'hazards': {
      title: 'Pipeline Hazards',
      icon: <Info className="text-red-400" size={24} />,
      content: `Data Hazards: When an instruction depends on a previous instruction's result. Solved by forwarding or stalling.\n\nControl Hazards: Uncertainty about which instruction to fetch next due to branches. Solved by prediction or flushing.\n\nStructural Hazards: Hardware resource conflicts (rare in MIPS).`,
      color: 'red'
    },
    'forwarding': {
      title: 'Data Forwarding',
      icon: <Zap className="text-yellow-400" size={24} />,
      content: `Forwarding (bypassing) allows results from EX/MEM or MEM/WB registers to be used directly in the EX stage, avoiding pipeline stalls.\n\nEX-to-EX: Forward from EX/MEM (ForwardA/B = 2)\nMEM-to-EX: Forward from MEM/WB (ForwardA/B = 1)`,
      color: 'yellow'
    }
  };

  const stages = [
    { name: 'IF', color: 'bg-blue-500', label: 'Fetch' },
    { name: 'ID', color: 'bg-green-500', label: 'Decode' },
    { name: 'EX', color: 'bg-yellow-500', label: 'Execute' },
    { name: 'MEM', color: 'bg-orange-500', label: 'Memory' },
    { name: 'WB', color: 'bg-purple-500', label: 'Write' }
  ];

  const pipelineRegisters = [
    { name: 'IF/ID', data: stageDataView.IF, color: 'border-blue-500' },
    { name: 'ID/EX', data: stageDataView.ID, color: 'border-green-500' },
    { name: 'EX/MEM', data: stageDataView.EX, color: 'border-yellow-500' },
    { name: 'MEM/WB', data: stageDataView.MEM, color: 'border-orange-500' }
  ];

  const exData = stageDataView.EX;
  const hasForwarding = exData.forwardA > 0 || exData.forwardB > 0;

  return (
    <div className="space-y-6">
      {/* Learning Topics Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        {Object.keys(educationalContent).map(key => (
          <button
            key={key}
            onClick={() => setSelectedTopic(key)}
            className={`p-3 rounded-lg text-left transition-all hover:scale-105 ${
              selectedTopic === key
                ? `bg-${educationalContent[key].color}-600 border-2 border-white`
                : 'bg-slate-800 hover:bg-slate-700 border border-slate-600'
            }`}
          >
            <div className="flex items-center gap-2">
              {educationalContent[key].icon}
              <span className="text-sm font-semibold">{educationalContent[key].title}</span>
            </div>
          </button>
        ))}
      </div>

      {/* Educational Content Panel */}
      {selectedTopic && (
        <div className="bg-slate-800 border-2 border-purple-500 rounded-lg p-5 animate-fade-in">
          <div className="flex justify-between items-start mb-3">
            <div className="flex items-center gap-3">
              {educationalContent[selectedTopic].icon}
              <h3 className="text-xl font-bold text-purple-300">
                {educationalContent[selectedTopic].title}
              </h3>
            </div>
            <button
              onClick={() => setSelectedTopic(null)}
              className="text-slate-400 hover:text-white transition-colors"
            >
              <X size={20} />
            </button>
          </div>
          <p className="text-slate-300 leading-relaxed whitespace-pre-line mb-4">
            {educationalContent[selectedTopic].content}
          </p>
          {educationalContent[selectedTopic].signals && (
            <div className="flex flex-wrap gap-2">
              <span className="text-xs text-slate-400 font-semibold">Key Signals:</span>
              {educationalContent[selectedTopic].signals.map((signal, idx) => (
                <span key={idx} className="px-2 py-1 bg-slate-700 border border-slate-600 rounded text-xs">
                  {signal}
                </span>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Pipeline Stages - Horizontal Flow */}
      <div className="bg-slate-900 rounded-lg border-2 border-slate-700 p-6">
        <h3 className="text-lg font-bold text-purple-400 mb-4">Pipeline Stages</h3>
        
        <div className="flex items-center justify-between mb-6">
          {stages.map((stage, index) => {
            const stageData = stageDataView[stage.name];
            const hasInstr = hasInstruction(stageData);

            return (
              <React.Fragment key={stage.name}>
                <div 
                  className={`${stage.color} rounded-lg p-4 w-32 text-center cursor-pointer hover:scale-105 transition-all ${
                    hasInstr ? 'opacity-100' : 'opacity-40'
                  }`}
                  onClick={() => setSelectedTopic(stage.name)}
                >
                  <div className="font-bold text-white text-xl mb-1">{stage.name}</div>
                  <div className="text-xs text-white/80 mb-2">{stage.label}</div>
                  {hasInstr && (
                    <div className="text-xs text-white/90 font-mono">
                      {stageData.instruction?.assembly?.split(' ')[0] || 'INSTR'}
                    </div>
                  )}
                </div>
                {index < stages.length - 1 && (
                  <ArrowRight className="text-purple-400" size={28} />
                )}
              </React.Fragment>
            );
          })}
        </div>

        {/* Pipeline Registers */}
        <div className="grid grid-cols-4 gap-4 mt-6">
          {pipelineRegisters.map(reg => {
            const hasData = hasInstruction(reg.data);
            return (
              <div key={reg.name} className={`bg-slate-800 ${reg.color} border-2 rounded-lg p-3`}>
                <div className="flex justify-between items-center mb-2">
                  <span className="text-xs font-bold text-white">{reg.name}</span>
                  <div className={`w-2 h-2 rounded-full ${hasData ? 'bg-green-400' : 'bg-slate-600'}`}></div>
                </div>
                {hasData ? (
                  <div className="text-xs font-mono text-slate-300">
                    {reg.data.instruction?.assembly?.substring(0, 12) || reg.data.instruction?.hex?.substring(0, 10)}...
                  </div>
                ) : (
                  <div className="text-xs text-slate-500 italic">Empty</div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Pipeline Register Details */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* IF/ID Register */}
        <div className="bg-slate-800 border border-blue-500 rounded-lg p-4">
          <h4 className="font-bold text-blue-400 mb-3">IF/ID Register</h4>
          {hasInstruction(stageDataView.IF) ? (
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-slate-400">PC:</span>
                <span className="text-blue-400 font-mono">{stageDataView.IF.pc}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Instruction:</span>
                <span className="text-green-400 font-mono text-xs">{stageDataView.IF.instruction?.hex}</span>
              </div>
            </div>
          ) : (
            <div className="text-sm text-slate-500 italic">No instruction</div>
          )}
        </div>

        {/* ID/EX Register */}
        <div className="bg-slate-800 border border-green-500 rounded-lg p-4">
          <h4 className="font-bold text-green-400 mb-3">ID/EX Register</h4>
          {hasInstruction(stageDataView.ID) ? (
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-slate-400">Source Regs:</span>
                <span className="text-green-400 font-mono">
                  {REGISTER_NAMES[stageDataView.ID.rs]}, {REGISTER_NAMES[stageDataView.ID.rt]}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Data Values:</span>
                <span className="text-cyan-400 font-mono">
                  {stageDataView.ID.readData1}, {stageDataView.ID.readData2}
                </span>
              </div>
              {stageDataView.ID.signExtImm !== 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-400">Immediate:</span>
                  <span className="text-yellow-400 font-mono">{stageDataView.ID.signExtImm}</span>
                </div>
              )}
            </div>
          ) : (
            <div className="text-sm text-slate-500 italic">No instruction</div>
          )}
        </div>

        {/* EX/MEM Register */}
        <div className="bg-slate-800 border border-yellow-500 rounded-lg p-4">
          <h4 className="font-bold text-yellow-400 mb-3">EX/MEM Register</h4>
          {hasInstruction(stageDataView.EX) ? (
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-slate-400">ALU Result:</span>
                <span className="text-orange-400 font-mono">{stageDataView.EX.aluResult}</span>
              </div>
              {stageDataView.EX.destReg >= 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-400">Destination:</span>
                  <span className="text-purple-400 font-mono">{REGISTER_NAMES[stageDataView.EX.destReg]}</span>
                </div>
              )}
              {(exData.forwardA > 0 || exData.forwardB > 0) && (
                <div className="mt-2 pt-2 border-t border-slate-700">
                  <div className="text-yellow-400 font-bold mb-1">Forwarding:</div>
                  {exData.forwardA > 0 && <div className="text-xs">A: {exData.forwardA === 2 ? 'EX/MEM' : 'MEM/WB'}</div>}
                  {exData.forwardB > 0 && <div className="text-xs">B: {exData.forwardB === 2 ? 'EX/MEM' : 'MEM/WB'}</div>}
                </div>
              )}
            </div>
          ) : (
            <div className="text-sm text-slate-500 italic">No instruction</div>
          )}
        </div>

        {/* MEM/WB Register */}
        <div className="bg-slate-800 border border-orange-500 rounded-lg p-4">
          <h4 className="font-bold text-orange-400 mb-3">MEM/WB Register</h4>
          {hasInstruction(stageDataView.MEM) ? (
            <div className="space-y-2 text-sm">
              {stageDataView.MEM.memData !== undefined && (
                <div className="flex justify-between">
                  <span className="text-slate-400">Memory Data:</span>
                  <span className="text-green-400 font-mono">{stageDataView.MEM.memData}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-slate-400">Write Data:</span>
                <span className="text-green-400 font-mono">{stageDataView.MEM.writeData}</span>
              </div>
              {stageDataView.MEM.destReg >= 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-400">Destination:</span>
                  <span className="text-purple-400 font-mono">{REGISTER_NAMES[stageDataView.MEM.destReg]}</span>
                </div>
              )}
            </div>
          ) : (
            <div className="text-sm text-slate-500 italic">No instruction</div>
          )}
        </div>
      </div>

      {/* Active Control Signals */}
      <div className="bg-slate-800 border border-slate-600 rounded-lg p-4">
        <h4 className="font-bold text-purple-400 mb-3">Active Control Signals</h4>
        <div className="flex flex-wrap gap-2">
          {stageDataView.ID.regWrite && (
            <span className="px-3 py-1.5 bg-green-600/30 border border-green-500 rounded text-sm">RegWrite</span>
          )}
          {stageDataView.EX.memRead && (
            <span className="px-3 py-1.5 bg-blue-600/30 border border-blue-500 rounded text-sm">MemRead</span>
          )}
          {stageDataView.EX.memWrite && (
            <span className="px-3 py-1.5 bg-red-600/30 border border-red-500 rounded text-sm">MemWrite</span>
          )}
          {stageDataView.EX.branch && (
            <span className="px-3 py-1.5 bg-yellow-600/30 border border-yellow-500 rounded text-sm">Branch</span>
          )}
          {stageDataView.EX.branchTaken && (
            <span className="px-3 py-1.5 bg-orange-600 border border-orange-500 rounded text-sm animate-pulse">Branch Taken!</span>
          )}
          {stageDataView.WB.memToReg && (
            <span className="px-3 py-1.5 bg-purple-600/30 border border-purple-500 rounded text-sm">MemToReg</span>
          )}
          {hasForwarding && (
            <span className="px-3 py-1.5 bg-yellow-600 border border-yellow-500 rounded text-sm animate-pulse flex items-center gap-1">
              <Zap size={14} />
              Forwarding Active
            </span>
          )}
          {!stageDataView.ID.regWrite && !stageDataView.EX.memRead && !stageDataView.EX.memWrite && !hasForwarding && (
            <span className="px-3 py-1.5 bg-slate-700 text-slate-400 rounded text-sm">No active signals</span>
          )}
        </div>
      </div>

      {/* Usage Guide */}
      <div className="bg-blue-900/20 border border-blue-500/50 rounded-lg p-4">
        <div className="flex items-start gap-2">
          <Info className="text-blue-400 mt-0.5" size={20} />
          <div>
            <div className="font-bold text-blue-400 mb-2">How to Use:</div>
            <ul className="text-sm text-slate-300 space-y-1">
              <li>• Click on any topic button to learn about pipeline concepts</li>
              <li>• Click on stage boxes to see detailed explanations</li>
              <li>• Watch pipeline registers fill with instruction data as you step</li>
              <li>• Monitor control signals to understand instruction flow</li>
              <li>• Look for "Forwarding Active" when data hazards are resolved</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}