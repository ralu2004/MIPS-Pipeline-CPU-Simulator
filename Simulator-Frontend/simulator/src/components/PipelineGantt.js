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
  const maxCycles = history.length;

  const getInstructionMnemonic = (stageInfo) => {
    if (!stageInfo || !stageInfo.instruction) return '';
    
    const instr = stageInfo.instruction;
    
    if (instr && typeof instr === 'object' && instr.assembly) {
      return instr.assembly.split(' ')[0];
    }
    
    const instructionStr = stageInfo.instructionStr || JSON.stringify(instr);
    
    if (instructionStr.includes('RTypeInstruction')) {
      const funcMatch = instructionStr.match(/func=(\d+)/);
      if (funcMatch) {
        const func = parseInt(funcMatch[1]);
        switch(func) {
          case 32: return 'add';
          case 34: return 'sub';
          case 36: return 'and';
          case 37: return 'or';
          case 42: return 'slt';
          case 8: return 'jr';
          default: return `R${func}`;
        }
      }
    } else if (instructionStr.includes('ITypeInstruction')) {
      if (instructionStr.includes('immediate=2')) return 'beq';
      if (instructionStr.includes('immediate=4')) return 'beq';
      return 'I-type';
    }
    
    return 'INSTR';
  };

  return (
    <div className="bg-slate-900 rounded-lg p-4 overflow-x-auto">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-bold text-purple-300">📊 Pipeline Gantt Chart</h3>
        <span className="text-xs text-slate-400">{maxCycles} cycles</span>
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
                  {cycleIdx + 1}
                </td>
                {stages.map(stage => {
                  const stageInfo = snapshot[stage];
                  
                  let cellClass = 'border border-slate-700 p-2 text-center font-mono';
                  let cellContent = '';
                  let bgColor = '';
                  let title = '';

                  if (!stageInfo) {
                    // EMPTY
                    cellContent = 'EMPTY';
                    bgColor = '#1e293b';
                    title = 'Empty';
                  } else if (stageInfo.state === 'INSTR' && stageInfo.instruction) {
                    // INSTRUCTION
                    cellClass += ' text-white font-semibold';
                    cellContent = getInstructionMnemonic(stageInfo);
                    bgColor = '#3b82f6';
                    
                    const fullAssembly = stageInfo.instruction?.assembly;
                    title = fullAssembly ? `Instruction: ${fullAssembly}` : `Instruction: ${cellContent}`;
                  } else if (stageInfo.state === 'STALL') {
                    // STALL
                    cellClass += ' text-white font-bold';
                    cellContent = 'STALL';
                    bgColor = '#f59e0b';
                    title = 'Stall';
                  } else if (stageInfo.state === 'BUBBLE') {
                    // BUBBLE
                    cellClass += ' text-purple-300';
                    cellContent = 'BUBBLE';
                    bgColor = '#7c3aed';
                    title = 'Bubble';
                  } else if (stageInfo.state === 'FLUSH') {
                    // FLUSH
                    cellClass += ' text-white font-bold';
                    cellContent = 'FLUSH';
                    bgColor = '#ef4444';
                    title = 'Flush';
                  } else {
                    cellContent = stageInfo.state || 'EMPTY';
                    bgColor = '#1e293b';
                    title = stageInfo.state || 'Empty';
                  }

                  return (
                    <td
                      key={stage}
                      className={cellClass}
                      style={{ backgroundColor: bgColor }}
                      title={title}
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

      <div className="mt-4 text-xs text-slate-400">
        <p>Each row shows the pipeline state for a single cycle across all stages</p>
      </div>

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
          <span className="text-slate-300">Flush</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-4 rounded" style={{ backgroundColor: '#f59e0b' }}></div>
          <span className="text-slate-300">Stall</span>
        </div>
      </div>
    </div>
  );
}