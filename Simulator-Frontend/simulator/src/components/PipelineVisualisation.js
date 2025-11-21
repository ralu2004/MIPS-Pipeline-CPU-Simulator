import { STAGE_INFO, REGISTER_NAMES } from "./Constants";

export default function PipelineVisualization({ pipeline }) {
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