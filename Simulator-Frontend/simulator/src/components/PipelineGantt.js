import React from "react";

export default function PipelineGantt({ history }) {
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