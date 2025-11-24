import { STAGE_INFO, REGISTER_NAMES } from "./Constants";

export default function PipelineVisualization({ pipeline, currentSnapshot }) {

  // Now both modes have full stage data with pipeline registers
  const stageDataView = currentSnapshot ? {
    IF:  currentSnapshot.IF  ?? {},
    ID:  currentSnapshot.ID  ?? {},
    EX:  currentSnapshot.EX  ?? {},
    MEM: currentSnapshot.MEM ?? {},
    WB:  currentSnapshot.WB  ?? {}
  } : {
    IF:  pipeline.IF_ID  ?? {},
    ID:  pipeline.ID_EX  ?? {},
    EX:  pipeline.EX_MEM ?? {},
    MEM: pipeline.MEM_WB ?? {},
    WB:  {}  
  };

  const stages = [
    { name: 'IF',  key: 'IF',  label: 'Instruction Fetch' },
    { name: 'ID',  key: 'ID',  label: 'Instruction Decode' },
    { name: 'EX',  key: 'EX',  label: 'Execute' },
    { name: 'MEM', key: 'MEM', label: 'Memory Access' },
    { name: 'WB',  key: 'WB',  label: 'Write Back' }
  ];

  return (
    <div className="space-y-3">
      {stages.map((stage, index) => {
        const stageData = stageDataView[stage.key];

        const hasInstruction =
          stageData?.instruction &&
          stageData.instruction.hex &&
          stageData.instruction.hex !== "null";

        return (
          <div key={stage.name} className="relative group">
            <div
              className={`bg-slate-900 rounded-lg p-4 border-2 transition-all ${
                hasInstruction
                  ? "border-purple-500 shadow-lg shadow-purple-500/20"
                  : "border-slate-700"
              }`}
            >
              <div className="flex justify-between items-center mb-2">
                <div className="relative">
                  <span className="font-bold text-lg text-purple-400">
                    {stage.name}
                  </span>
                  <span className="text-xs text-slate-400 ml-2">
                    {stage.label}
                  </span>

                  <div className="absolute left-0 top-8 w-72 bg-slate-800 border border-purple-500 rounded-lg p-3 text-xs text-slate-300 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-10 shadow-xl">
                    <div className="font-bold text-purple-400 mb-1">
                      {stage.name} Stage
                    </div>
                    {STAGE_INFO[stage.name]}
                  </div>
                </div>

                <span
                  className={`px-2 py-1 rounded text-xs font-semibold ${
                    hasInstruction ? "bg-green-500" : "bg-slate-700"
                  }`}
                >
                  {hasInstruction ? "ACTIVE" : "EMPTY"}
                </span>
              </div>

              {hasInstruction ? (
                <div className="space-y-1">
                  <div className="font-mono text-sm text-slate-300">
                    {stageData.instruction.hex}
                  </div>

                  {stageData.instruction.assembly && (
                    <div className="font-mono text-xs text-green-300">
                      {stageData.instruction.assembly}
                    </div>
                  )}

                  {/* Stage-specific meaningful data */}
                  {stage.name === "IF" && (
                    <>
                      {stageData.pc !== undefined && (
                        <div className="text-xs text-slate-400">
                          PC: {stageData.pc}
                        </div>
                      )}
                    </>
                  )}

                  {stage.name === "ID" && (
                    <>
                      {stageData.readData1 !== undefined && (
                        <div className="text-xs text-blue-300">
                          Read Data 1: {stageData.readData1}
                        </div>
                      )}
                      {stageData.readData2 !== undefined && (
                        <div className="text-xs text-blue-300">
                          Read Data 2: {stageData.readData2}
                        </div>
                      )}
                      {stageData.signExtImm !== undefined && (
                        <div className="text-xs text-cyan-300">
                          Imm: {stageData.signExtImm}
                        </div>
                      )}
                      {stageData.regWrite !== undefined && (
                        <div className="text-xs text-green-300">
                          RegWrite: {stageData.regWrite ? 'ON' : 'OFF'}
                        </div>
                      )}
                    </>
                  )}

                  {stage.name === "EX" && (
                    <>
                      {stageData.aluResult !== undefined && (
                        <div className="text-xs text-orange-300">
                          ALU Result: {stageData.aluResult}
                        </div>
                      )}
                      {stageData.destReg !== undefined && stageData.destReg >= 0 && (
                        <div className="text-xs text-purple-300">
                          Dest: {REGISTER_NAMES[stageData.destReg] ?? stageData.destReg}
                        </div>
                      )}
                      {stageData.zero !== undefined && (
                        <div className="text-xs text-yellow-300">
                          Zero: {stageData.zero ? 'true' : 'false'}
                        </div>
                      )}
                      {stageData.branchTaken !== undefined && (
                        <div className="text-xs text-red-300">
                          Branch Taken: {stageData.branchTaken ? 'YES' : 'NO'}
                        </div>
                      )}
                    </>
                  )}

                  {stage.name === "MEM" && (
                    <>
                      {stageData.aluResult !== undefined && (
                        <div className="text-xs text-orange-300">
                          ALU Result: {stageData.aluResult}
                        </div>
                      )}
                      {stageData.memData !== undefined && (
                        <div className="text-xs text-green-300">
                          Mem Data: {stageData.memData}
                        </div>
                      )}
                      {stageData.destReg !== undefined && stageData.destReg >= 0 && (
                        <div className="text-xs text-purple-300">
                          Dest: {REGISTER_NAMES[stageData.destReg] ?? stageData.destReg}
                        </div>
                      )}
                      {stageData.memRead !== undefined && stageData.memRead && (
                        <div className="text-xs text-blue-300">MemRead: ON</div>
                      )}
                      {stageData.memWrite !== undefined && stageData.memWrite && (
                        <div className="text-xs text-red-300">MemWrite: ON</div>
                      )}
                    </>
                  )}

                  {stage.name === "WB" && (
                    <>
                      {stageData.destReg !== undefined && stageData.destReg >= 0 && (
                        <div className="text-xs text-purple-300">
                          Writing to: {REGISTER_NAMES[stageData.destReg] ?? stageData.destReg}
                        </div>
                      )}
                      {stageData.regWrite !== undefined && stageData.regWrite && (
                        <div className="text-xs text-green-300">RegWrite: ON</div>
                      )}
                      {stageData.memToReg !== undefined && (
                        <div className="text-xs text-cyan-300">
                          MemToReg: {stageData.memToReg ? 'ON' : 'OFF'}
                        </div>
                      )}
                    </>
                  )}
                </div>
              ) : (
                <div className="text-sm text-slate-500 italic">
                  No instruction
                </div>
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