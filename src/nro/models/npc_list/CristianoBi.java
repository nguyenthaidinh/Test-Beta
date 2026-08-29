package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;

/** NPC Cristiano Bi tại Làng Kakarot. */
public final class CristianoBi extends Npc {

    private static final int STORY_FRAME_1 = 86_001;
    private static final int STORY_FRAME_2 = 86_002;
    private static final int STORY_FRAME_3 = 86_003;

    public CristianoBi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Ngươi muốn nghe câu chuyện của Cristiano Bi không?",
                    "Nói\nchuyện", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        switch (player.idMark.getIndexMenu()) {
            case ConstNpc.BASE_MENU -> {
                if (select == 0) {
                    createOtherMenu(player, STORY_FRAME_1,
                            "Xương rồng đơm lá đơn hoa.", "Tiếp");
                }
            }
            case STORY_FRAME_1 -> {
                if (select == 0) {
                    createOtherMenu(player, STORY_FRAME_2,
                            "Nước cao đầy trên cao nguyên đá.", "Tiếp");
                }
            }
            case STORY_FRAME_2 -> {
                if (select == 0) {
                    createOtherMenu(player, STORY_FRAME_3,
                            "Là ngày Bi Con là trùm.", "Đóng");
                }
            }
            case STORY_FRAME_3 -> {
                // Khung cuối chỉ đóng hội thoại.
            }
            default -> {
            }
        }
    }
}
