// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : MemoryEcho.java
//  Bagian  : Puzzle, memory, quest (data "gema memori" pada sebuah benda)
//  PJ      : Azzam Insan Kamil
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.memory;

/**
 * Jejak memori yang menempel di sebuah benda. Bisa dibaca The Remnant.
 * Gema = petunjuk cerita. Fragmen (3 buah) didapat dari puzzle/quest di W3.
 */
public final class MemoryEcho {

    private final String id;
    private final String objectName;
    private final String spriteKey;
    private final String inspectDialogueId;
    private final String echoDialogueId;
    private final String journalTitle;
    private final String journalText;

    public MemoryEcho(String id, String objectName, String spriteKey, String inspectDialogueId,
                      String echoDialogueId, String journalTitle, String journalText) {
        this.id = id;
        this.objectName = objectName;
        this.spriteKey = spriteKey;
        this.inspectDialogueId = inspectDialogueId;
        this.echoDialogueId = echoDialogueId;
        this.journalTitle = journalTitle;
        this.journalText = journalText;
    }

    public String getId() { return id; }
    public String getObjectName() { return objectName; }
    public String getSpriteKey() { return spriteKey; }
    /** Dialog saat diperiksa TANPA The Remnant. */
    public String getInspectDialogueId() { return inspectDialogueId; }
    /** Dialog kilas balik saat dipindai The Remnant. */
    public String getEchoDialogueId() { return echoDialogueId; }
    public String getJournalTitle() { return journalTitle; }
    public String getJournalText() { return journalText; }
}
