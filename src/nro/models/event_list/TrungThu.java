package nro.models.event_list;

import nro.models.boss.BossID;
import nro.models.consts.ConstNpc;
import nro.models.event.Event;

public class TrungThu extends Event {

    @Override
    public void npc() {
        // Đặt NPC Nồi Bánh tại 3 làng chính
        createNpc(0, ConstNpc.NOI_BANH, 450, 432);   // Làng Trái Đất
        createNpc(7, ConstNpc.NOI_BANH, 450, 432);   // Thành phố Vegeta
        createNpc(14, ConstNpc.NOI_BANH, 450, 432);  // Làng Namek
    }

    @Override
    public void boss() {
        createBoss(BossID.KHIDOT, 10);
    }
}
