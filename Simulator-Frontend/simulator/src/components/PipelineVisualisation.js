import React, { useState } from 'react';
import { AlertCircle, ArrowRight, Zap, AlertTriangle, Clock, Info, ChevronDown, ChevronRight } from 'lucide-react';
import { REGISTER_NAMES} from './Constants';

export default function PipelineVisualization({ pipeline, currentSnapshot }) {
  const [showControlSignals, setShowControlSignals] = useState(true);
  const [showHazards, setShowHazards] = useState(true);
  const [expandedStage, setExpandedStage] = useState(null);

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

  const stages = [
    { name: 'IF', key: 'IF', label: 'Instruction Fetch', color: 'blue' },
    { name: 'ID', key: 'ID', label: 'Instruction Decode', color: 'green' },
    { name: 'EX', key: 'EX', label: 'Execute', color: 'yellow' },
    { name: 'MEM', key: 'MEM', label: 'Memory Access', color: 'orange' },
    { name: 'WB', key: 'WB', label: 'Write Back', color: 'purple' }
  ];

  const getColorClass = (color, variant = 'bg') => {
    const colorMap = {
      blue: { bg: 'bg-blue-500', border: 'border-blue-500', text: 'text-blue-400', light: 'bg-blue-500/20' },
      green: { bg: 'bg-green-500', border: 'border-green-500', text: 'text-green-400', light: 'bg-green-500/20' },
      yellow: { bg: 'bg-yellow-500', border: 'border-yellow-500', text: 'text-yellow-400', light: 'bg-yellow-500/20' },
      orange: { bg: 'bg-orange-500', border: 'border-orange-500', text: 'text-orange-400', light: 'bg-orange-500/20' },
      purple: { bg: 'bg-purple-500', border: 'border-purple-500', text: 'text-purple-400', light: 'bg-purple-500/20' }
    };
    return colorMap[color]?.[variant] || colorMap.blue[variant];
  };

  const hasInstruction = (stageData) => {
    return stageData?.instruction?.hex && 
           stageData.instruction.hex !== 'null' && 
           stageData.instruction.hex !== '0x00000000';
  };

  const detectHazards = () => {
    const hazards = [];

    const exStage = stageDataView.EX;
    if (exStage.forwardA > 0 || exStage.forwardB > 0) {
      hazards.push({
        type: 'data',
        severity: 'info',
        title: 'Data Hazard Resolved by Forwarding',
        details: [
          exStage.forwardA > 0 && `Input A forwarded from ${exStage.forwardA === 2 ? 'EX/MEM' : 'MEM/WB'}`,
          exStage.forwardB > 0 && `Input B forwarded from ${exStage.forwardB === 2 ? 'EX/MEM' : 'MEM/WB'}`
        ].filter(Boolean)
      });
    }

    if (exStage.branch && hasInstruction(exStage)) {
      hazards.push({
        type: 'control',
        severity: exStage.branchTaken ? 'warning' : 'info',
        title: exStage.branchTaken ? 'Branch Taken - Pipeline Flush' : 'Branch Not Taken',
        details: [
          `Branch instruction detected in EX stage`,
          exStage.branchTaken ? 'Instructions in IF and ID will be flushed' : 'Continue sequential execution'
        ]
      });
    }

    stages.forEach(stage => {
      const stageData = stageDataView[stage.key];
      if (stageData.state === 'STALL') {
        hazards.push({
          type: 'structural',
          severity: 'warning',
          title: `Pipeline Stall in ${stage.name}`,
          details: [`${stage.name} stage is stalled due to resource conflict`]
        });
      }
    });

    return hazards;
  };

  const hazards = showHazards ? detectHazards() : [];

  const getControlSignals = (stageKey) => {
    const stageData = stageDataView[stageKey];
    const signals = [];

    if (stageKey === 'ID' || stageKey === 'EX') {
      if (stageData.regWrite) signals.push({ name: 'RegWrite', value: '1', color: 'green' });
      if (stageData.memRead) signals.push({ name: 'MemRead', value: '1', color: 'blue' });
      if (stageData.memWrite) signals.push({ name: 'MemWrite', value: '1', color: 'red' });
      if (stageData.branch) signals.push({ name: 'Branch', value: '1', color: 'yellow' });
      if (stageData.memToReg) signals.push({ name: 'MemToReg', value: '1', color: 'purple' });
    }

    if (stageKey === 'MEM') {
      if (stageData.regWrite) signals.push({ name: 'RegWrite', value: '1', color: 'green' });
      if (stageData.memRead) signals.push({ name: 'MemRead', value: '1', color: 'blue' });
      if (stageData.memWrite) signals.push({ name: 'MemWrite', value: '1', color: 'red' });
      if (stageData.memToReg) signals.push({ name: 'MemToReg', value: '1', color: 'purple' });
    }

    if (stageKey === 'WB') {
      if (stageData.regWrite) signals.push({ name: 'RegWrite', value: '1', color: 'green' });
      if (stageData.memToReg) signals.push({ name: 'MemToReg', value: '1', color: 'purple' });
    }

    return signals;
  };

const StageCard = ({ stage, index }) => {
  const stageData = stageDataView[stage.key];
  const hasInstr = hasInstruction(stageData);
  const isExpanded = expandedStage === stage.name;
  const controlSignals = getControlSignals(stage.key);

  const stageState = stageData?.state;
  const isStall = stageState === 'STALL';
  const isBubble = stageState === 'BUBBLE';
  const isFlush = stageState === 'FLUSH';

  return (
    <div className="relative group">
      <div 
        className={`rounded-lg border-2 transition-all ${
          hasInstr ? `${getColorClass(stage.color, 'border')} bg-slate-800` : 'border-slate-700 bg-slate-900'
        } ${isStall ? 'animate-pulse' : ''} ${isBubble || isFlush ? 'opacity-50' : ''}`}
      >
        <div className="p-4">
          <div className="flex justify-between items-start mb-2">
            <div className="flex items-center gap-2 relative">
              {/* Tooltip container with group-hover */}
              <div className="relative">
                <div 
                  className="flex items-center gap-2 cursor-pointer"
                  onClick={() => setExpandedStage(isExpanded ? null : stage.name)}
                >
                  <div className={`w-3 h-3 rounded-full ${getColorClass(stage.color, 'bg')}`}></div>
                  <div>
                    <span className={`font-bold text-lg ${getColorClass(stage.color, 'text')}`}>
                      {stage.name}
                    </span>
                    <span className="text-xs text-slate-400 ml-2">{stage.label}</span>
                  </div>
                </div>
                
                
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <span className={`px-2 py-1 rounded text-xs font-semibold ${
                hasInstr ? 'bg-green-500 text-white' : 
                isStall ? 'bg-yellow-500 text-black' :
                isBubble ? 'bg-purple-500 text-white' :
                isFlush ? 'bg-red-500 text-white' :
                'bg-slate-700 text-slate-400'
              }`}>
                {hasInstr ? 'ACTIVE' : isStall ? 'STALL' : isBubble ? 'BUBBLE' : isFlush ? 'FLUSH' : 'EMPTY'}
              </span>
              <div 
                className="cursor-pointer"
                onClick={() => setExpandedStage(isExpanded ? null : stage.name)}
              >
                {isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
              </div>
            </div>
          </div>

          {hasInstr && (
            <div 
              className="space-y-1 cursor-pointer"
              onClick={() => setExpandedStage(isExpanded ? null : stage.name)}
            >
              <div className="font-mono text-sm text-slate-300">
                {stageData.instruction.hex}
              </div>
              {stageData.instruction.assembly && (
                <div className="font-mono text-xs text-green-400">
                  {stageData.instruction.assembly}
                </div>
              )}
            </div>
          )}
        </div>

        {isExpanded && hasInstr && (
          <div className="border-t border-slate-700 p-4 space-y-3 bg-slate-900/50">
            <div className="space-y-2">
              {stage.name === 'IF' && stageData.pc !== undefined && (
                <div className="text-xs">
                  <span className="text-slate-400">Program Counter:</span>
                  <span className="text-blue-400 ml-2 font-mono">{stageData.pc}</span>
                </div>
              )}

              {stage.name === 'ID' && (
                <>
                  {stageData.rs !== undefined && (
                    <div className="text-xs">
                      <span className="text-slate-400">Source Reg (rs):</span>
                      <span className="text-green-400 ml-2 font-mono">
                        {REGISTER_NAMES[stageData.rs]} = {stageData.readData1 ?? '?'}
                      </span>
                    </div>
                  )}
                  {stageData.rt !== undefined && (
                    <div className="text-xs">
                      <span className="text-slate-400">Target Reg (rt):</span>
                      <span className="text-green-400 ml-2 font-mono">
                        {REGISTER_NAMES[stageData.rt]} = {stageData.readData2 ?? '?'}
                      </span>
                    </div>
                  )}
                  {stageData.signExtImm !== undefined && stageData.signExtImm !== 0 && (
                    <div className="text-xs">
                      <span className="text-slate-400">Immediate:</span>
                      <span className="text-cyan-400 ml-2 font-mono">{stageData.signExtImm}</span>
                    </div>
                  )}
                </>
              )}

              {stage.name === 'EX' && (
                <>
                  {stageData.aluResult !== undefined && (
                    <div className="text-xs">
                      <span className="text-slate-400">ALU Result:</span>
                      <span className="text-orange-400 ml-2 font-mono">{stageData.aluResult}</span>
                    </div>
                  )}
                  {stageData.destReg >= 0 && (
                    <div className="text-xs">
                      <span className="text-slate-400">Destination:</span>
                      <span className="text-purple-400 ml-2 font-mono">{REGISTER_NAMES[stageData.destReg]}</span>
                    </div>
                  )}
                  {stageData.zero !== undefined && (
                    <div className="text-xs">
                      <span className="text-slate-400">Zero Flag:</span>
                      <span className={`ml-2 font-mono ${stageData.zero ? 'text-yellow-400' : 'text-slate-500'}`}>
                        {stageData.zero ? 'TRUE' : 'FALSE'}
                      </span>
                    </div>
                  )}
                  
                  {(stageData.forwardA > 0 || stageData.forwardB > 0) && (
                    <div className="mt-2 p-2 bg-yellow-900/30 border border-yellow-500/50 rounded">
                      <div className="flex items-center gap-1 text-yellow-400 text-xs font-bold mb-1">
                        <Zap size={12} />
                        Forwarding Active
                      </div>
                      {stageData.forwardA > 0 && (
                        <div className="text-xs text-orange-300">
                          → Input A from {stageData.forwardA === 2 ? 'EX/MEM' : 'MEM/WB'}
                        </div>
                      )}
                      {stageData.forwardB > 0 && (
                        <div className="text-xs text-orange-300">
                          → Input B from {stageData.forwardB === 2 ? 'EX/MEM' : 'MEM/WB'}
                        </div>
                      )}
                    </div>
                  )}
                </>
              )}

              {stage.name === 'MEM' && (
                <>
                  {stageData.aluResult !== undefined && (
                    <div className="text-xs">
                      <span className="text-slate-400">Address:</span>
                      <span className="text-orange-400 ml-2 font-mono">{stageData.aluResult}</span>
                    </div>
                  )}
                  {stageData.memData !== undefined && (
                    <div className="text-xs">
                      <span className="text-slate-400">Memory Data:</span>
                      <span className="text-green-400 ml-2 font-mono">{stageData.memData}</span>
                    </div>
                  )}
                  {stageData.destReg >= 0 && (
                    <div className="text-xs">
                      <span className="text-slate-400">Destination:</span>
                      <span className="text-purple-400 ml-2 font-mono">{REGISTER_NAMES[stageData.destReg]}</span>
                    </div>
                  )}
                </>
              )}

              {stage.name === 'WB' && (
                <>
                  {stageData.writeData !== undefined && (
                    <div className="text-xs">
                      <span className="text-slate-400">Write Data:</span>
                      <span className="text-green-400 ml-2 font-mono">{stageData.writeData}</span>
                    </div>
                  )}
                  {stageData.destReg >= 0 && (
                    <div className="text-xs">
                      <span className="text-slate-400">Writing to:</span>
                      <span className="text-purple-400 ml-2 font-mono">{REGISTER_NAMES[stageData.destReg]}</span>
                    </div>
                  )}
                </>
              )}
            </div>

            {showControlSignals && controlSignals.length > 0 && (
              <div className="pt-2 border-t border-slate-700">
                <div className="text-xs font-bold text-slate-400 mb-2">Control Signals:</div>
                <div className="flex flex-wrap gap-1">
                  {controlSignals.map((signal, idx) => (
                    <span 
                      key={idx}
                      className={`px-2 py-1 rounded text-xs font-mono ${
                        signal.color === 'green' ? 'bg-green-600/30 text-green-400 border border-green-500/50' :
                        signal.color === 'blue' ? 'bg-blue-600/30 text-blue-400 border border-blue-500/50' :
                        signal.color === 'red' ? 'bg-red-600/30 text-red-400 border border-red-500/50' :
                        signal.color === 'yellow' ? 'bg-yellow-600/30 text-yellow-400 border border-yellow-500/50' :
                        'bg-purple-600/30 text-purple-400 border border-purple-500/50'
                      }`}
                    >
                      {signal.name}={signal.value}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {index < stages.length - 1 && (
        <div className="flex justify-center my-2">
          <ArrowRight className="text-purple-400" size={20} />
        </div>
      )}
    </div>
  );
};

  const HazardAlerts = () => {
    if (hazards.length === 0) return null;

    return (
      <div className="space-y-3 mb-6">
        {hazards.map((hazard, idx) => (
          <div 
            key={idx}
            className={`p-4 rounded-lg border-2 ${
              hazard.severity === 'warning' 
                ? 'bg-yellow-900/20 border-yellow-500' 
                : hazard.severity === 'error'
                ? 'bg-red-900/20 border-red-500'
                : 'bg-blue-900/20 border-blue-500'
            }`}
          >
            <div className="flex items-start gap-2">
              {hazard.type === 'data' && <Zap className="text-blue-400 mt-0.5" size={18} />}
              {hazard.type === 'control' && <AlertTriangle className="text-yellow-400 mt-0.5" size={18} />}
              {hazard.type === 'structural' && <Clock className="text-orange-400 mt-0.5" size={18} />}
              <div className="flex-1">
                <div className="font-bold text-sm mb-1">
                  {hazard.type === 'data' && '⚡ Data Hazard'}
                  {hazard.type === 'control' && '🔀 Control Hazard'}
                  {hazard.type === 'structural' && '⏸️ Structural Hazard'}
                  <span className="ml-2 text-slate-300">{hazard.title}</span>
                </div>
                <div className="space-y-1">
                  {hazard.details.map((detail, i) => (
                    <div key={i} className="text-xs text-slate-300">• {detail}</div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-end gap-2">
        <button
          onClick={() => setShowControlSignals(!showControlSignals)}
          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-1 ${
            showControlSignals 
              ? 'bg-purple-600 text-white' 
              : 'bg-slate-700 text-slate-400 hover:bg-slate-600'
          }`}
        >
          <Info size={14} />
          Control Signals
        </button>
        <button
          onClick={() => setShowHazards(!showHazards)}
          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-1 ${
            showHazards 
              ? 'bg-yellow-600 text-white' 
              : 'bg-slate-700 text-slate-400 hover:bg-slate-600'
          }`}
        >
          <AlertCircle size={14} />
          Show Hazards
        </button>
      </div>

      <HazardAlerts />

      <div className="space-y-2">
        {stages.map((stage, index) => (
          <StageCard key={stage.name} stage={stage} index={index} />
        ))}
      </div>

      <div className="bg-slate-800/50 rounded-lg p-4 border border-slate-700">
        <div className="text-xs font-bold text-slate-400 mb-3">Legend</div>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-3 text-xs">
          <div>
            <div className="font-semibold text-slate-300 mb-1">Stage States:</div>
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 bg-green-500 rounded"></div>
                <span>Active</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 bg-yellow-500 rounded"></div>
                <span>Stall</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 bg-purple-500 rounded"></div>
                <span>Bubble</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 bg-red-500 rounded"></div>
                <span>Flush</span>
              </div>
            </div>
          </div>
          <div>
            <div className="font-semibold text-slate-300 mb-1">Hazard Types:</div>
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <Zap size={14} className="text-blue-400" />
                <span>Data Hazard</span>
              </div>
              <div className="flex items-center gap-2">
                <AlertTriangle size={14} className="text-yellow-400" />
                <span>Control Hazard</span>
              </div>
              <div className="flex items-center gap-2">
                <Clock size={14} className="text-orange-400" />
                <span>Structural Hazard</span>
              </div>
            </div>
          </div>
          <div>
            <div className="font-semibold text-slate-300 mb-1">Control Signals:</div>
            <div className="space-y-1">
              <div className="px-2 py-0.5 bg-green-600/30 text-green-400 border border-green-500/50 rounded inline-block">RegWrite</div>
              <div className="px-2 py-0.5 bg-blue-600/30 text-blue-400 border border-blue-500/50 rounded inline-block">MemRead</div>
              <div className="px-2 py-0.5 bg-red-600/30 text-red-400 border border-red-500/50 rounded inline-block">MemWrite</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}