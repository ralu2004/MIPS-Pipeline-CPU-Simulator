import React from 'react';
import { REGISTER_NAMES } from './Constants';

export default function PipelineDiagram({ pipeline }) {
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