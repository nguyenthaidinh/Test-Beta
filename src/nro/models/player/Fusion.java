package nro.models.player;

import nro.models.item.Item;
import nro.models.consts.ConstPlayer;
import lombok.Setter;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class Fusion {

    public static final int TIME_FUSION = 600000;

    @Setter
    private Player player;
    public byte typeFusion;
    public long lastTimeFusion;
    /** Thời điểm tạo dùng làm định danh của đúng Bông tai Porata người chơi đã bấm. */
    public long selectedPorataCreateTime;
    public Fusion(Player player) {
        this.player = player;
    }

    public void update() {
        if (typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE && Util.canDoWithTime(lastTimeFusion, TIME_FUSION)) {
            this.player.pet.unFusion();
        }
    }

    public void selectPorata(Item item) {
        if (item != null && item.isNotNullItem()) {
            selectedPorataCreateTime = item.createTime;
        }
    }

    public Item getSelectedPorata(int templateId) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return null;
        }
        if (selectedPorataCreateTime > 0) {
            for (Item item : player.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == templateId
                        && item.createTime == selectedPorataCreateTime) {
                    return item;
                }
            }
        }
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == templateId) {
                selectedPorataCreateTime = item.createTime;
                return item;
            }
        }
        return null;
    }
    
    public void dispose(){
        this.player = null;
    }

}
