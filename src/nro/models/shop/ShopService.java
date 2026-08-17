package nro.models.shop;

import nro.models.consts.ConstAchievement;
import nro.models.consts.ConstItem;
import nro.models.item.Item;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.radar.Card;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;
import nro.models.shop.TabShop;
import nro.models.network.Message;
import nro.models.item.Item.ItemOption;
import java.util.ArrayList;
import nro.models.server.Manager;
import nro.models.services.GoldBarSpendService;
import nro.models.services.InventoryService;
import nro.models.utils.Logger;
import nro.models.utils.Util;
import java.util.List;
import nro.models.npc.MagicTree;
import nro.models.player_badges.BadgesData;
import nro.models.player_badges.BadgesService;
import nro.models.player_badges.BagesTemplate;
import nro.models.services.AchievementService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services_func.Input;
import nro.models.services_func.BuyBackService;
import nro.models.services_func.UseItem;
import nro.models.map.service.NpcService;
import nro.models.task.BadgesTaskService;
import nro.models.utils.SkillUtil;
import nro.models.utils.TimeUtil;

/**
 *
 * @author By Mr Blue
 *
 */
public class ShopService {

    private static final byte COST_GOLD = 0;
    private static final byte COST_GEM = 1;
    private static final byte COST_RUBY = 3;
    private static final byte COST_COUPON = 4;

    private static final byte NORMAL_SHOP = 0;
    private static final byte SPEC_SHOP = 3;
    private static final byte KINANG_SHOP = 1;
    private static final String SHOP_CHI_CHI = "SHOP_CHI_CHI";
    private static final String HALLOWEEN_EVENT_SHOP = "HALLOWEEN_EVENT_SHOP";
    private static final int TAB_CHI_CHI_EVENT_ID = 58;
    private static final short GOLD_BAR_ITEM_ID = (short) ConstItem.THOI_VANG;
    private static final int GOLD_BAR_ICON_ID = 4028;
    private static final short GOLD_CURRENCY_ITEM_ID = 76;
    private static final short GEM_CURRENCY_ITEM_ID = 77;
    private static final short RUBY_CURRENCY_ITEM_ID = (short) ConstItem.HONG_NGOC;
    private static final int CHI_CHI_GEM_PACKAGE_SHOP_ID = -1_000_077;
    private static final int CHI_CHI_GEM_PACKAGE_AMOUNT = 1_000_000;
    private static final int CHI_CHI_GEM_PACKAGE_GOLD_BAR_COST = 15_000;
    private static final short SOUL_DETECTOR_ITEM_ID = (short) ConstItem.MAY_DO_LINH_HON;
    private static final int SOUL_DETECTOR_GOLD_BAR_COST = 100;
    private static final short DEVIL_CANDY_BOX_ITEM_ID = (short) ConstItem.HOP_KEO_MA_QUY;
    private static final int DEVIL_CANDY_BOX_GOLD_BAR_COST = 200;
    private static final short HUY_DIET_CAPSULE_ITEM_ID = (short) ConstItem.HOP_CAPSULE;
    private static final int HUY_DIET_CAPSULE_GOLD_BAR_COST = 10_000;
    private static final short TRUM_TOP_1_ITEM_ID = 1870;
    private static final int TRUM_TOP_1_GEM_COST = 1_200_000;
    private static final short[] SSJ4_COSTUME_ITEM_IDS = {1553, 1693};
    private static final int SSJ4_COSTUME_GEM_COST = 500_000;
    private static final short JACKY_CHUN_COSTUME_ITEM_ID = (short) ConstItem.CAI_TRANG_JACKY_CHUN;
    private static final int JACKY_CHUN_COSTUME_GEM_COST = 3_000_000;
    private static final int JACKY_CHUN_COSTUME_EXPIRE_DAYS = 7;
    private static final int OPTION_EXPIRE_DAYS = 93;
    private static final short[] FEATURED_EVENT_ITEM_IDS = {1780, 1781, 1722, 1784, 1783};
    private static final int[] FEATURED_EVENT_ITEM_GEM_COSTS = {3_000_000, 3_000_000, 1_000_000, 800_000, 600_000};
    private static final long BADGE_GOLD_COST = 16_000_000_000L;
    private static final short OOZARUN_1_CARD_ID = 1792;
    private static final short OOZARUN_2_CARD_ID = 1793;
    private static final byte RADAR_CARD_MAX_LEVEL = 2;
    private int eventPointPrice;

    private static ShopService I;

    public static ShopService gI() {
        if (ShopService.I == null) {
            ShopService.I = new ShopService();
        }
        return ShopService.I;
    }

    public void opendShop(Player player, String tagName, boolean allGender) {
        if (tagName.equals("ITEMS_LUCKY_ROUND")) {
            openShopType4(player, tagName, player.inventory.itemsBoxCrackBall);
            return;
        } else if (tagName.equals("ITEMS_DABAN")) {
            openShopType8(player, tagName, player.inventory.itemsDaBan);
            return;
        }
        try {
            Shop shop = this.getShop(tagName);
            if (SHOP_CHI_CHI.equals(tagName)) {
                ensureTrumTop1InChiChiShop(shop);
                ensureSsj4CostumesInChiChiShop(shop);
                ensureJackyChunCostumeInChiChiShop(shop);
                ensureFeaturedEventItemsInChiChiShop(shop);
                hideGoldBarInChiChiShop(shop);
                ensureGemPackageInChiChiShop(shop);
                configureChiChiSpecialShop(shop);
            } else if (HALLOWEEN_EVENT_SHOP.equals(tagName)) {
                ensureSoulDetectorInHalloweenEventShop(shop);
                ensureDevilCandyBoxInHalloweenEventShop(shop);
                ensureHuyDietCapsuleInHalloweenEventShop(shop);
            }
            for (TabShop tabShop : shop.tabShops) {
                for (ItemShop item : tabShop.itemShops) {
                    switch (item.temp.id) {
                        case 1627:// hành trang
                            if (player.inventory.itemsBag.size() >= 35) {
                                item.cost = ((player.inventory.itemsBag.size() - 35) + 1) * 2;
                            } else {
                                item.cost = 1;
                            }
                            break;
                    }
                }
            }
            shop = this.resolveShop(player, shop, allGender);
            switch (shop.typeShop) {
                case KINANG_SHOP:
                    openShopType1(player, shop);
                    break;
                case NORMAL_SHOP:
                    openShopType0(player, shop);
                    break;
                case SPEC_SHOP:
                    openShopType3(player, shop);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Service.gI().sendThongBao(player, ex.getMessage());
        }
    }

    private Shop getShop(String tagName) throws Exception {
        for (Shop s : Manager.SHOPS) {
            if (s.tagName != null && s.tagName.equals(tagName)) {
                return s;
            }
        }
        throw new Exception("Shop " + tagName + " không tồn tại!");
    }

    private Shop resolveShop(Player player, Shop shop, boolean allGender) {
        if (shop.tagName != null && (shop.tagName.equals("BUA_1H") || shop.tagName.equals("BUA_8H") || shop.tagName.equals("BUA_1M"))) {
            return this.resolveShopBua(player, new Shop(shop));
        }
        return allGender ? new Shop(shop) : new Shop(shop, player);
    }

    private void ensureTrumTop1InChiChiShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        for (TabShop tabShop : shop.tabShops) {
            for (ItemShop itemShop : tabShop.itemShops) {
                if (itemShop.temp != null && itemShop.temp.id == TRUM_TOP_1_ITEM_ID) {
                    configureTrumTop1ShopItem(itemShop, tabShop);
                    return;
                }
            }
        }
        TabShop eventTab = null;
        for (TabShop tabShop : shop.tabShops) {
            if (tabShop.id == TAB_CHI_CHI_EVENT_ID) {
                eventTab = tabShop;
                break;
            }
        }
        if (eventTab == null && !shop.tabShops.isEmpty()) {
            eventTab = shop.tabShops.get(0);
        }
        if (eventTab == null) {
            return;
        }
        ItemShop trumTop1 = new ItemShop();
        trumTop1.id = -TRUM_TOP_1_ITEM_ID;
        if (!configureTrumTop1ShopItem(trumTop1, eventTab)) {
            return;
        }
        eventTab.itemShops.add(0, trumTop1);
    }

    private boolean configureTrumTop1ShopItem(ItemShop itemShop, TabShop eventTab) {
        itemShop.tabShop = eventTab;
        itemShop.temp = ItemService.gI().getTemplate(TRUM_TOP_1_ITEM_ID);
        if (itemShop.temp == null) {
            return false;
        }
        ItemService.gI().normalizeTrumTop1Template(itemShop.temp);
        itemShop.isNew = true;
        itemShop.typeSell = COST_GEM;
        itemShop.cost = TRUM_TOP_1_GEM_COST;
        itemShop.iconSpec = 0;
        itemShop.options.clear();
        itemShop.options.addAll(ItemService.gI().getTrumTop1Options());
        return true;
    }

    private void ensureSsj4CostumesInChiChiShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        TabShop eventTab = null;
        for (TabShop tabShop : shop.tabShops) {
            if (tabShop.id == TAB_CHI_CHI_EVENT_ID) {
                eventTab = tabShop;
                break;
            }
        }
        if (eventTab == null && !shop.tabShops.isEmpty()) {
            eventTab = shop.tabShops.get(0);
        }
        if (eventTab == null) {
            return;
        }

        int insertIndex = 1;
        for (short costumeId : SSJ4_COSTUME_ITEM_IDS) {
            ItemShop costumeShopItem = null;
            for (ItemShop itemShop : eventTab.itemShops) {
                if (itemShop.temp != null && itemShop.temp.id == costumeId) {
                    costumeShopItem = itemShop;
                    break;
                }
            }
            boolean isNewShopItem = costumeShopItem == null;
            if (costumeShopItem == null) {
                costumeShopItem = new ItemShop();
                costumeShopItem.id = -costumeId;
            }
            if (!configureSsj4CostumeShopItem(costumeShopItem, eventTab, costumeId)) {
                continue;
            }
            if (isNewShopItem) {
                eventTab.itemShops.add(Math.min(insertIndex, eventTab.itemShops.size()), costumeShopItem);
            }
            insertIndex++;
        }
    }

    private boolean configureSsj4CostumeShopItem(ItemShop itemShop, TabShop eventTab, short costumeId) {
        itemShop.tabShop = eventTab;
        itemShop.temp = ItemService.gI().getTemplate(costumeId);
        if (itemShop.temp == null) {
            return false;
        }
        itemShop.isNew = true;
        itemShop.typeSell = COST_GEM;
        itemShop.cost = SSJ4_COSTUME_GEM_COST;
        itemShop.iconSpec = 0;
        itemShop.options.clear();
        itemShop.options.addAll(ItemService.gI().getSsj4CostumeOptions());
        return true;
    }

    private void ensureJackyChunCostumeInChiChiShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        TabShop eventTab = null;
        for (TabShop tabShop : shop.tabShops) {
            if (tabShop.id == TAB_CHI_CHI_EVENT_ID) {
                eventTab = tabShop;
                break;
            }
        }
        if (eventTab == null && !shop.tabShops.isEmpty()) {
            eventTab = shop.tabShops.get(0);
        }
        if (eventTab == null) {
            return;
        }

        ItemShop jackyChunShopItem = null;
        for (ItemShop itemShop : eventTab.itemShops) {
            if (itemShop.temp != null && itemShop.temp.id == JACKY_CHUN_COSTUME_ITEM_ID) {
                jackyChunShopItem = itemShop;
                break;
            }
        }
        boolean isNewShopItem = jackyChunShopItem == null;
        if (jackyChunShopItem == null) {
            jackyChunShopItem = new ItemShop();
            jackyChunShopItem.id = -JACKY_CHUN_COSTUME_ITEM_ID;
        }
        if (!configureJackyChunCostumeShopItem(jackyChunShopItem, eventTab)) {
            return;
        }
        if (isNewShopItem) {
            eventTab.itemShops.add(Math.min(3, eventTab.itemShops.size()), jackyChunShopItem);
        }
    }

    private boolean configureJackyChunCostumeShopItem(ItemShop itemShop, TabShop eventTab) {
        itemShop.tabShop = eventTab;
        itemShop.temp = ItemService.gI().getTemplate(JACKY_CHUN_COSTUME_ITEM_ID);
        if (itemShop.temp == null) {
            return false;
        }
        ItemService.gI().normalizeJackyChunCostumeTemplate(itemShop.temp);
        itemShop.isNew = true;
        itemShop.typeSell = COST_GEM;
        itemShop.cost = JACKY_CHUN_COSTUME_GEM_COST;
        itemShop.iconSpec = 0;
        itemShop.options.clear();
        itemShop.options.addAll(ItemService.gI().getJackyChunCostumeOptions());
        itemShop.options.add(new ItemOption(OPTION_EXPIRE_DAYS, JACKY_CHUN_COSTUME_EXPIRE_DAYS));
        return true;
    }

    private void ensureFeaturedEventItemsInChiChiShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        TabShop eventTab = null;
        for (TabShop tabShop : shop.tabShops) {
            if (tabShop.id == TAB_CHI_CHI_EVENT_ID) {
                eventTab = tabShop;
                break;
            }
        }
        if (eventTab == null && !shop.tabShops.isEmpty()) {
            eventTab = shop.tabShops.get(0);
        }
        if (eventTab == null) {
            return;
        }

        int insertIndex = 4;
        for (int i = 0; i < FEATURED_EVENT_ITEM_IDS.length; i++) {
            short itemId = FEATURED_EVENT_ITEM_IDS[i];
            int gemCost = FEATURED_EVENT_ITEM_GEM_COSTS[i];
            ItemShop eventShopItem = null;
            for (ItemShop itemShop : eventTab.itemShops) {
                if (itemShop.temp != null && itemShop.temp.id == itemId) {
                    eventShopItem = itemShop;
                    break;
                }
            }
            boolean isNewShopItem = eventShopItem == null;
            if (eventShopItem == null) {
                eventShopItem = new ItemShop();
                eventShopItem.id = -itemId;
            }
            if (!configureFeaturedEventShopItem(eventShopItem, eventTab, itemId, gemCost)) {
                continue;
            }
            if (isNewShopItem) {
                eventTab.itemShops.add(Math.min(insertIndex, eventTab.itemShops.size()), eventShopItem);
            }
            insertIndex++;
        }
    }

    private boolean configureFeaturedEventShopItem(ItemShop itemShop, TabShop eventTab, short itemId, int gemCost) {
        Item item = ItemService.gI().createNewItem(itemId);
        if (item == null || item.template == null) {
            return false;
        }
        itemShop.tabShop = eventTab;
        itemShop.temp = item.template;
        itemShop.isNew = true;
        itemShop.typeSell = COST_GEM;
        itemShop.cost = gemCost;
        itemShop.iconSpec = 0;
        itemShop.options.clear();
        for (ItemOption option : item.itemOptions) {
            itemShop.options.add(new ItemOption(option));
        }
        return true;
    }

    private void hideGoldBarInChiChiShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        for (TabShop tabShop : shop.tabShops) {
            tabShop.itemShops.removeIf(itemShop -> itemShop != null
                    && itemShop.temp != null
                    && itemShop.temp.id == GOLD_BAR_ITEM_ID);
        }
    }

    private void ensureGemPackageInChiChiShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        TabShop eventTab = null;
        for (TabShop tabShop : shop.tabShops) {
            if (tabShop.id == TAB_CHI_CHI_EVENT_ID) {
                eventTab = tabShop;
                break;
            }
        }
        if (eventTab == null && !shop.tabShops.isEmpty()) {
            eventTab = shop.tabShops.get(0);
        }
        if (eventTab == null) {
            return;
        }

        ItemShop gemPackage = null;
        for (ItemShop itemShop : eventTab.itemShops) {
            if (itemShop.id == CHI_CHI_GEM_PACKAGE_SHOP_ID) {
                gemPackage = itemShop;
                break;
            }
        }
        boolean isNewShopItem = gemPackage == null;
        if (gemPackage == null) {
            gemPackage = new ItemShop();
            gemPackage.id = CHI_CHI_GEM_PACKAGE_SHOP_ID;
        }
        if (!configureGemPackageShopItem(gemPackage, eventTab)) {
            return;
        }
        if (isNewShopItem) {
            eventTab.itemShops.add(0, gemPackage);
        }
    }

    private boolean configureGemPackageShopItem(ItemShop itemShop, TabShop eventTab) {
        itemShop.tabShop = eventTab;
        itemShop.temp = ItemService.gI().getTemplate(GEM_CURRENCY_ITEM_ID);
        if (itemShop.temp == null) {
            return false;
        }
        itemShop.isNew = true;
        itemShop.typeSell = COST_GEM;
        itemShop.cost = CHI_CHI_GEM_PACKAGE_GOLD_BAR_COST;
        itemShop.iconSpec = GOLD_BAR_ICON_ID;
        itemShop.options.clear();
        return true;
    }

    private void configureChiChiSpecialShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        shop.typeShop = SPEC_SHOP;
        for (TabShop tabShop : shop.tabShops) {
            for (ItemShop itemShop : tabShop.itemShops) {
                if (itemShop == null || itemShop.temp == null
                        || itemShop.id == CHI_CHI_GEM_PACKAGE_SHOP_ID) {
                    continue;
                }
                short currencyItemId;
                switch (itemShop.typeSell) {
                    case COST_GOLD:
                        currencyItemId = GOLD_CURRENCY_ITEM_ID;
                        break;
                    case COST_GEM:
                        currencyItemId = GEM_CURRENCY_ITEM_ID;
                        break;
                    case COST_RUBY:
                        currencyItemId = RUBY_CURRENCY_ITEM_ID;
                        break;
                    default:
                        continue;
                }
                if (ItemService.gI().getTemplate(currencyItemId) != null) {
                    itemShop.iconSpec = ItemService.gI().getTemplate(currencyItemId).iconID;
                }
            }
        }
    }

    private void ensureSoulDetectorInHalloweenEventShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        shop.typeShop = SPEC_SHOP;
        TabShop eventTab = shop.tabShops.isEmpty() ? null : shop.tabShops.get(0);
        if (eventTab == null) {
            return;
        }
        ItemShop soulDetector = null;
        for (ItemShop itemShop : eventTab.itemShops) {
            if (itemShop.temp != null && itemShop.temp.id == SOUL_DETECTOR_ITEM_ID) {
                soulDetector = itemShop;
                break;
            }
        }
        boolean isNewShopItem = soulDetector == null;
        if (soulDetector == null) {
            soulDetector = new ItemShop();
            soulDetector.id = -SOUL_DETECTOR_ITEM_ID;
        }
        if (!configureSoulDetectorShopItem(soulDetector, eventTab)) {
            return;
        }
        if (isNewShopItem) {
            eventTab.itemShops.add(0, soulDetector);
        }
    }

    private boolean configureSoulDetectorShopItem(ItemShop itemShop, TabShop eventTab) {
        itemShop.tabShop = eventTab;
        itemShop.temp = ItemService.gI().getTemplate(SOUL_DETECTOR_ITEM_ID);
        if (itemShop.temp == null) {
            return false;
        }
        ItemService.gI().normalizePumpkinCandyTemplate(itemShop.temp);
        itemShop.isNew = true;
        itemShop.typeSell = COST_GEM;
        itemShop.cost = SOUL_DETECTOR_GOLD_BAR_COST;
        itemShop.iconSpec = GOLD_BAR_ICON_ID;
        itemShop.options.clear();
        return true;
    }

    private void ensureDevilCandyBoxInHalloweenEventShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        shop.typeShop = SPEC_SHOP;
        TabShop eventTab = shop.tabShops.isEmpty() ? null : shop.tabShops.get(0);
        if (eventTab == null) {
            return;
        }
        ItemShop devilCandyBox = null;
        for (ItemShop itemShop : eventTab.itemShops) {
            if (itemShop.temp != null && itemShop.temp.id == DEVIL_CANDY_BOX_ITEM_ID) {
                devilCandyBox = itemShop;
                break;
            }
        }
        boolean isNewShopItem = devilCandyBox == null;
        if (devilCandyBox == null) {
            devilCandyBox = new ItemShop();
            devilCandyBox.id = -DEVIL_CANDY_BOX_ITEM_ID;
        }
        if (!configureDevilCandyBoxShopItem(devilCandyBox, eventTab)) {
            return;
        }
        if (isNewShopItem) {
            eventTab.itemShops.add(Math.min(1, eventTab.itemShops.size()), devilCandyBox);
        }
    }

    private boolean configureDevilCandyBoxShopItem(ItemShop itemShop, TabShop eventTab) {
        itemShop.tabShop = eventTab;
        itemShop.temp = ItemService.gI().getTemplate(DEVIL_CANDY_BOX_ITEM_ID);
        if (itemShop.temp == null) {
            return false;
        }
        ItemService.gI().normalizePumpkinCandyTemplate(itemShop.temp);
        itemShop.isNew = true;
        itemShop.typeSell = COST_GEM;
        itemShop.cost = DEVIL_CANDY_BOX_GOLD_BAR_COST;
        itemShop.iconSpec = GOLD_BAR_ICON_ID;
        itemShop.options.clear();
        return true;
    }

    private void ensureHuyDietCapsuleInHalloweenEventShop(Shop shop) {
        if (shop == null || shop.tabShops == null) {
            return;
        }
        shop.typeShop = SPEC_SHOP;
        TabShop eventTab = shop.tabShops.isEmpty() ? null : shop.tabShops.get(0);
        if (eventTab == null) {
            return;
        }
        ItemShop huyDietCapsule = null;
        for (ItemShop itemShop : eventTab.itemShops) {
            if (itemShop.temp != null && itemShop.temp.id == HUY_DIET_CAPSULE_ITEM_ID) {
                huyDietCapsule = itemShop;
                break;
            }
        }
        boolean isNewShopItem = huyDietCapsule == null;
        if (huyDietCapsule == null) {
            huyDietCapsule = new ItemShop();
            huyDietCapsule.id = -HUY_DIET_CAPSULE_ITEM_ID;
        }
        if (!configureHuyDietCapsuleShopItem(huyDietCapsule, eventTab)) {
            return;
        }
        if (isNewShopItem) {
            eventTab.itemShops.add(Math.min(2, eventTab.itemShops.size()), huyDietCapsule);
        }
    }

    private boolean configureHuyDietCapsuleShopItem(ItemShop itemShop, TabShop eventTab) {
        itemShop.tabShop = eventTab;
        itemShop.temp = ItemService.gI().getTemplate(HUY_DIET_CAPSULE_ITEM_ID);
        if (itemShop.temp == null) {
            return false;
        }
        ItemService.gI().normalizePumpkinCandyTemplate(itemShop.temp);
        itemShop.isNew = true;
        itemShop.typeSell = COST_GEM;
        itemShop.cost = HUY_DIET_CAPSULE_GOLD_BAR_COST;
        itemShop.iconSpec = GOLD_BAR_ICON_ID;
        itemShop.options.clear();
        return true;
    }

    private Shop resolveShopBua(Player player, Shop s) {
        for (TabShop tabShop : s.tabShops) {
            for (ItemShop item : tabShop.itemShops) {
                long min = 0;
                switch (item.temp.id) {
                    case 213:
                        long timeTriTue = player.charms.tdTriTue;
                        long current = System.currentTimeMillis();
                        min = (timeTriTue - current) / 60000;

                        break;
                    case 214:
                        min = (player.charms.tdManhMe - System.currentTimeMillis()) / 60000;
                        break;
                    case 215:
                        min = (player.charms.tdDaTrau - System.currentTimeMillis()) / 60000;
                        break;
                    case 216:
                        min = (player.charms.tdOaiHung - System.currentTimeMillis()) / 60000;
                        break;
                    case 217:
                        min = (player.charms.tdBatTu - System.currentTimeMillis()) / 60000;
                        break;
                    case 218:
                        min = (player.charms.tdDeoDai - System.currentTimeMillis()) / 60000;
                        break;
                    case 219:
                        min = (player.charms.tdThuHut - System.currentTimeMillis()) / 60000;
                        break;
                    case 522:
                        min = (player.charms.tdDeTu - System.currentTimeMillis()) / 60000;
                        break;
                    case 671:
                        min = (player.charms.tdTriTue3 - System.currentTimeMillis()) / 60000;
                        break;
                    case 672:
                        min = (player.charms.tdTriTue4 - System.currentTimeMillis()) / 60000;
                        break;
                }
                if (min > 0) {
                    item.options.clear();
                    if (min >= 1440) {
                        item.options.add(new Item.ItemOption(63, (int) min / 1440));
                    } else if (min >= 60) {
                        item.options.add(new Item.ItemOption(64, (int) min / 60));
                    } else {
                        item.options.add(new Item.ItemOption(65, (int) min));
                    }
                }
            }
        }
        return s;
    }

    private List<Item.ItemOption> getClientShopOptions(List<Item.ItemOption> options) {
        List<Item.ItemOption> clientOptions = new ArrayList<>();
        if (options == null) {
            return clientOptions;
        }
        boolean addedDoThanThanhDisplay = false;
        for (Item.ItemOption option : options) {
            if (option == null || option.optionTemplate == null) {
                continue;
            }
            if (ItemService.isDoThanThanhSetOption(option.optionTemplate.id, option.param)) {
                if (!addedDoThanThanhDisplay) {
                    Item.ItemOption displayOption = ItemService.getDoThanThanhDisplayOption(option.param);
                    if (displayOption != null && displayOption.optionTemplate != null) {
                        clientOptions.add(displayOption);
                        addedDoThanThanhDisplay = true;
                    }
                }
                continue;
            }
            if (ItemService.isDoThanThanhDisplayOption(option.optionTemplate.id)) {
                continue;
            }
            clientOptions.add(option);
        }
        return clientOptions;
    }

    private void openShopType0(Player player, Shop shop) {
        if (shop != null) {
            player.idMark.setShopOpen(shop);
            player.idMark.setTagNameShop(shop.tagName);
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(NORMAL_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        if (itemShop.typeSell == COST_GOLD) {
                            msg.writer().writeInt(itemShop.cost);
                            msg.writer().writeInt(0);
                        } else if (itemShop.typeSell == COST_GEM) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_RUBY) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_COUPON) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        }
                        List<Item.ItemOption> itemOptions = getClientShopOptions(itemShop.options);
                        msg.writer().writeByte(itemOptions.size());
                        for (Item.ItemOption option : itemOptions) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }
                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopDanhHieu(Player player, Shop shop) {
        if (shop != null) {
            player.idMark.setShopOpen(shop);
            player.idMark.setTagNameShop(shop.tagName);
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(NORMAL_SHOP);
                msg.writer().writeByte(2);
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        if (itemShop.typeSell == COST_GOLD) {
                            msg.writer().writeInt(itemShop.cost);
                            msg.writer().writeInt(0);
                        } else if (itemShop.typeSell == COST_GEM) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_RUBY) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_COUPON) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        }
                        List<Item.ItemOption> itemOptions = getClientShopOptions(itemShop.options);
                        msg.writer().writeByte(itemOptions.size());
                        for (Item.ItemOption option : itemOptions) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }

                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopType1(Player player, Shop shop) {
        if (shop != null) {
            player.idMark.setShopOpen(shop);
            player.idMark.setTagNameShop(shop.tagName);
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(KINANG_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        String[] subName = itemShop.temp.name.split("");
                        byte level = Byte.parseByte(subName[subName.length - 1]);

                        var skillTemplateId = SkillUtil.getTempSkillSkillByItemID(itemShop.temp.id);
                        var costPotential = SkillUtil.findSkillTemplate(skillTemplateId).skillss.stream()
                                .filter(s -> s.point == level)
                                .findFirst()
                                .map(s -> (int) s.powRequire) // Ép kiểu Long -> int
                                .orElse(0); // Giá trị mặc định là int
                        msg.writer().writeLong(costPotential);

                        List<Item.ItemOption> itemOptions = getClientShopOptions(itemShop.options);
                        msg.writer().writeByte(itemOptions.size());
                        for (Item.ItemOption option : itemOptions) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);

                        msg.writer().writeByte(0);

                    }
                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopType3(Player player, Shop shop) {
        player.idMark.setShopOpen(shop);
        player.idMark.setTagNameShop(shop.tagName);
        if (shop != null) {
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(SPEC_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        msg.writer().writeShort(itemShop.iconSpec);
                        msg.writer().writeInt(itemShop.cost);
                        List<Item.ItemOption> itemOptions = getClientShopOptions(itemShop.options);
                        msg.writer().writeByte(itemOptions.size());
                        for (Item.ItemOption option : itemOptions) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }
                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopType4(Player player, String tagName, List<Item> items) {
        if (items == null) {
            return;
        }
        player.idMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(4);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Phần\nthưởng");
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                ItemService.gI().normalizeGokuNgayXuaOptions(item);
                ItemService.gI().normalizeLuckyRoundPetOptions(item);
                msg.writer().writeShort(item.template.id);
                msg.writer().writeUTF("|7| LUCKY REWARD");
                List<Item.ItemOption> itemOptions = item.getClientItemOptions();
                msg.writer().writeByte(itemOptions.size() + 1);
                for (Item.ItemOption io : itemOptions) {
                    msg.writer().writeByte(io.optionTemplate.id);
                    msg.writer().writeShort(io.param);
                }
                //số lượng
                msg.writer().writeByte(31);
                msg.writer().writeShort(item.quantity);
                //
                msg.writer().writeByte(1);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void openShopType8(Player player, String tagName, List<Item> items) {
        if (items == null) {
            return;
        }
        player.idMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(8);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Mua lại");
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                int giamualaingoc = item.template.gem / 2;
                int giamualaivang = giamualaingoc == 0 ? (int) item.template.gold / 2 > 0 ? (int) item.template.gold / 2 : item.quantity * 100 : 0;
                msg.writer().writeShort(item.template.id);
                msg.writer().writeInt(giamualaivang);
                msg.writer().writeInt(giamualaingoc);
                msg.writer().writeInt(item.quantity);
                List<Item.ItemOption> itemOptions = item.getClientItemOptions();
                msg.writer().writeByte(itemOptions.size());
                for (Item.ItemOption io : itemOptions) {
                    msg.writer().writeByte(io.optionTemplate.id);
                    msg.writer().writeShort(io.param);
                }
                msg.writer().writeByte(0);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void takeItem(Player player, byte type, int tempId) {
        String tagName = player.idMark.getTagNameShop();
        if (tagName == null || tagName.length() <= 0) {
            return;
        }
        if (tagName.equals("ITEMS_LUCKY_ROUND")) {
            getItemSideBoxLuckyRound(player, player.inventory.itemsBoxCrackBall, type, tempId);
            return;
        } else if (tagName.equals("ITEMS_REWARD")) {
            return;
        } else if (tagName.equals("ITEMS_DABAN")) {
            buyItemDaBan(player, player.inventory.itemsDaBan, type, tempId);
            return;
        } else if (tagName.equals("BILL")) {
            buyItemHD(player, tempId);
            return;
        }

        if (player.idMark.getShopOpen() == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (tagName.equals("BUA_1H") || tagName.equals("BUA_8H") || tagName.equals("BUA_1M")) {
            buyItemBua(player, tempId);
        } else if (tagName.equals("SANTA_HEAD")) {
            Item itS = ItemService.gI().createNewItem((short) tempId);
            player.head = (short) itS.template.head;
            Service.gI().Send_Caitrang(player);
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đổi kiểu tóc thành công");
        } else {
            buyItem(player, tempId);
        }
        Service.gI().sendMoney(player);
    }

    private boolean subMoneyByItemShop(Player player, ItemShop is) {
        int gold = 0;
        int gem = 0;
        int ruby = 0;
        int coupon = 0;
        switch (is.typeSell) {
            case COST_GOLD ->
                gold = is.cost;
            case COST_GEM ->
                gem = is.cost;
            case COST_RUBY ->
                ruby = is.cost;
            case COST_COUPON ->
                coupon = is.cost;
        }
        if (player.inventory.gold < gold) {
            Service.gI().sendThongBao(player, "Bạn không có đủ vàng");
            return false;
        } else if (player.inventory.gem < gem) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
            return false;
        } else if (player.inventory.gem < ruby) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
            return false;
        } else if (player.inventory.coupon < coupon) {
            Service.gI().sendThongBao(player, "Bạn không có đủ điểm");
            return false;
        }
        player.inventory.gold -= gold;
        player.inventory.gem -= gem;
        player.inventory.ruby -= ruby;
        player.inventory.coupon -= coupon;
        return true;
    }

    private boolean subMoneyByItemShopV2(Player player, ItemShop is) {
        int gold = 0;
        int gem = 0;
        int ruby = 0;
        int coupon = 0;
        switch (is.typeSell) {
            case COST_GOLD ->
                gold = is.cost;
            case COST_GEM ->
                gem = is.cost;
            case COST_RUBY ->
                ruby = is.cost;
            case COST_COUPON ->
                coupon = is.cost;
        }
        if (player.inventory.gold < gold) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(player.inventory.gold - gold));
            return false;
        } else if (player.inventory.gem < gem) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ ngọc, còn thiếu " + Util.numberToMoney(player.inventory.gem - gem));
            return false;
        } else if (player.inventory.ruby < ruby) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ ngọc, còn thiếu " + Util.numberToMoney(player.inventory.ruby - ruby));
            return false;
        } else if (player.inventory.coupon < coupon) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ điểm, còn thiếu " + Util.numberToMoney(player.inventory.coupon - coupon));
            return false;
        }
        player.inventory.gold -= gold;
        player.inventory.gem -= gem;
        player.inventory.ruby -= ruby;
        player.inventory.coupon -= coupon;
        Service.gI().sendMoney(player);
        return true;
    }

    /**
     * Mua bùa
     *
     * @param player người chơi
     * @param itemTempId id template vật phẩm
     */
    private void buyItemBua(Player player, int itemTempId) {
        Shop shop = player.idMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }

        if (!subMoneyByItemShop(player, is)) {
            return;
        }
        InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
        InventoryService.gI().sendItemBags(player);
        opendShop(player, shop.tagName, true);
    }

    /**
     * Mua vật phẩm trong cửa hàng
     *
     * @param player người chơi
     * @param itemTempId id template vật phẩm
     */
    private void learnKyNang(Player pl, ItemShop is) {

        if (pl.nPoint.power < is.temp.strRequire) {
            Service.gI().sendThongBao(pl, "Sức mạnh của bạn không đủ");
            return;
        }
        if (pl.nPoint.tiemNang < is.cost) {
            Service.gI().sendThongBao(pl, "Bạn không đủ tiềm năng để học chiêu thức này");
            return;
        }
        var skillPlayer = pl.playerSkill.getSkillbyId(SkillUtil.getSkillByItemID(pl, is.temp.id).template.id);
        String[] subName = is.temp.name.split("");
        byte level = Byte.parseByte(subName[subName.length - 1]);
        if (skillPlayer != null) {

            if (skillPlayer.point >= level) {
                Service.gI().sendThongBao(pl, "Bạn đã học kỹ năng này rồi");
                return;

            }
            if (level - skillPlayer.point != 1) {
                Service.gI().sendThongBao(pl, "Bạn chưa thể học kỹ năng này");
                return;
            }
        }
        if (pl.BoughtSkill.contains(is.temp.id)) {
            Service.gI().sendThongBao(pl, "Bạn đã học kỹ năng này rồi");
            return;
        }
        ArrayList<String> menu = new ArrayList<>();
        menu.add("Yes");
        menu.add("No");
        String[] menus = menu.toArray(String[]::new);
        long[] time = new long[]{900000, 1800000, 3600000, 86400000, 259200000, 604800000, 1296000000};
        var timeStudy = "";
        var timeLong = time[level - 1];
        switch (level) {
            case 0:
            case 1:
            case 2:
                timeStudy = TimeUtil.convertMillisecondToMinute(timeLong);
                break;
            case 3:
                timeStudy = TimeUtil.convertMillisecondToHour(timeLong);
                break;
            default:
                timeStudy = TimeUtil.convertMillisecondToDay(timeLong);
                break;
        }
        var skillTemplateId = SkillUtil.getTempSkillSkillByItemID(is.temp.id);

        var potential = SkillUtil.findSkillTemplate(skillTemplateId).skillss.stream()
                .filter(s -> s.point == level)
                .findFirst()
                .map(s -> (int) s.powRequire) // Ép kiểu Long -> int
                .orElse(0); // Giá trị mặc định là int
        String text = "Con có muốn học kỹ năng " + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(is.temp.id)).name + " cấp " + level + "\nCần " + potential + " điểm tiềm năng và thời gian học là " + timeStudy;
        pl.LearnSkill.ItemTemplateSkillId = is.temp.id;
        pl.LearnSkill.Time = -1;
        pl.LearnSkill.Potential = potential;

        NpcService.gI().createMenuConMeo(pl, 671, NpcService.gI().getAvatar(13 + pl.gender), text, menus);
    }

    public void buyItem(Player player, int itemTempId) {
        Shop shop = player.idMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        int[][] listDauThan = {{13, 293}, {60, 294}, {61, 295}, {62, 296}, {63, 297}, {64, 298}, {65, 299}, {352, 596}, {523, 597}};

        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }

        if (isChiChiGemPackage(shop, is)) {
            buyChiChiGemPackage(player);
            return;
        }

        // Đổi bằng phiếu giảm giá
        if (is.tabShop.id == 30) {
            Item pGG = InventoryService.gI().findItem(player.inventory.itemsBag, 459);
            if (pGG != null) {
                Item item = ItemService.gI().createItemFromItemShop(is);
                InventoryService.gI().subQuantityItemsBag(player, pGG, 1);
                InventoryService.gI().addItemBag(player, item);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Đổi thành công " + is.temp.name);
            } else {
                Service.gI().sendThongBao(player, "Bạn không có phiếu giảm giá!");
            }
            return;
        }

        // Đổi danh hiệu
        if (is.tabShop.id == 44) {
            buyDanhHieu(player, is);
            return;
        }

        // Đổi danh hiệu khác
        if (is.tabShop.id == 45) {
            changeDanhHieu(player, is);
            return;
        }

        // Shop chipi
        if (is.tabShop.id == 49) {
            Item item = ItemService.gI().createItemFromItemShop(is);
            if (Util.isTrue(5, 100)) {
                item.itemOptions.add(new ItemOption(73, 0)); // HSD vĩnh viễn
            } else {
                item.itemOptions.add(new ItemOption(93, Util.nextInt(3, 7))); // HSD 3-7 ngày
            }
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            return;
        }

        // Shop kỹ năng
        if (shop.typeShop == ShopService.KINANG_SHOP) {
            learnKyNang(player, is);
            return;
        }

        // Hành trang đầy
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy");
            return;
        }

        // Yêu cầu có cải trang Quy Lão Kame
        if (itemTempId == ConstItem.CAI_TRANG_JACKY_CHUN
                && is.tabShop.id != TAB_CHI_CHI_EVENT_ID
                && !InventoryService.gI().findItemSkinQuyLaoKame(player)) {
            Service.gI().sendThongBao(player, "Bạn phải có cải trang thành Quy Lão Kame mới có thể đổi.");
            return;
        }

        // Kiểm tra auto train
        if (itemTempId == 1524 || itemTempId == 1523 || itemTempId == 521) {
            if (!checkAutoTrainPurchase(player, itemTempId)) {
                return;
            }
        }

        // Đổi bằng điểm sự kiện
        if (is.tabShop.id == 59) {
            int eventPointPrice = 0;

            switch (is.temp.id) {
                case 1567:
                case 1731:
                    eventPointPrice = 999;
                    break;
                case 1711:
                    eventPointPrice = 750;
                    break;
                case 1840:
                    eventPointPrice = 99;
                    break;
                case 1713:
                    eventPointPrice = 499;
                    break;
                case 1608:
                    eventPointPrice = 9;
                    break;
                case 1682:
                    eventPointPrice = 499;
                    break;
                case 1698:
                    eventPointPrice = 499;
                    break;
                case 1821:
                    eventPointPrice = 199;
                    break;
                case 1757:
                    eventPointPrice = 99;
                    break;
                case 1592:
                    eventPointPrice = 99;
                    break;
                default:
            }

            if (player.event.getEventPoint() < eventPointPrice) {
                Service.gI().sendThongBao(player, "Không đủ điểm sự kiện để mua vật phẩm này!");
                return;
            }

            player.event.subEventPoint(eventPointPrice);
            Item item = ItemService.gI().createItemFromItemShop(is);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Đã đổi " + is.temp.name + " bằng " + eventPointPrice + " điểm sự kiện.");
            return;
        }
        // Đổi bằng điểm Capsule Bang
        if (is.tabShop.id == 60 || is.tabShop.id == 61 || is.tabShop.id == 62) {
            int capsuleClanPointPrice = 0;

            switch (is.temp.id) {
                case 1794:
                case 1204:
                case 1423:
                case 1438:
                case 1439:
                case 987:
                case 1635:
                    capsuleClanPointPrice = 1;
                    break;
                case 1790:
                    capsuleClanPointPrice = 2;
                    break;
                case 1791:
                    capsuleClanPointPrice = 3;
                    break;
                case 1792:
                case OOZARUN_2_CARD_ID:
                    capsuleClanPointPrice = 4;
                    break;
                case 1634:
                    capsuleClanPointPrice = 10;
                    break;
                case 1620:
                case 1748:
                case 1750:
                case 1729:
                case 1727:
                case 1714:
                case 1683:
                case 1682:
                case 1668:
                case 1629:
                case 1630:
                case 1631:
                case 1573:
                case 1550:
                case 1551:
                case 1541:
                case 1563:
                case 1724:
                case 1733:
                case 1734:
                case 1749:
                    capsuleClanPointPrice = 50;
                    break;
                default:
                    capsuleClanPointPrice = -1;
                    break;
            }

            if (capsuleClanPointPrice <= 0) {
                Service.gI().sendThongBao(player, "Vật phẩm này không thể mua bằng điểm Capsule Bang.");
                return;
            }

            if (player.clan == null) {
                Service.gI().sendThongBao(player, "Bạn không có trong bang hội!");
                return;
            }

            if (is.temp.id == OOZARUN_2_CARD_ID && !hasMaxedOozarun1Card(player)) {
                Service.gI().sendThongBao(player, "Bạn cần sưu tầm Thẻ Oozarun 1 đạt cấp tối đa mới có thể đổi Thẻ Oozarun 2.");
                return;
            }

            if (player.clan.capsuleClan < capsuleClanPointPrice) {
                Service.gI().sendThongBao(player, "Bang hội không đủ điểm Capsule Bang để mua vật phẩm này!");
                return;
            }

            player.clan.capsuleClan -= capsuleClanPointPrice;

            Item item = ItemService.gI().createItemFromItemShop(is);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);

            Service.gI().sendThongBao(player, "Đã đổi " + is.temp.name + " bằng " + capsuleClanPointPrice + " điểm Capsule Bang của bang hội.");
            return;
        }

        // Shop thường
        boolean paidWithGoldBar = shop.typeShop == ShopService.SPEC_SHOP && isGoldBarCost(is);

        if (shop.typeShop == ShopService.NORMAL_SHOP) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }
        } // Shop đặc biệt
        else if (shop.typeShop == ShopService.SPEC_SHOP) {
            if (!this.subIemByItemShop(player, is)) {
                return;
            }
        }

        // Tạo item và xử lý đặc biệt
        Item item = ItemService.gI().createItemFromItemShop(is);
        item = buyMagicPean(player, listDauThan, item);
        if (item.template.id == 1523 || item.template.id == 1524) {
            item = ItemService.gI().createNewItem((short) 521);
            item.itemOptions.addAll(is.options);
        }
        // Mảnh vỡ/Hồn bông tai: giới hạn 100 mảnh/ngày + cần option 31 để tích lũy đúng
        if (item.template.id == 933 || item.template.id == 934 || item.template.id == 935) {
            // Reset counter nếu qua ngày mới
            if (Util.isAfterMidnight(player.lastTimeFragmentBought)) {
                player.dailyFragmentBought = 0;
            }
            if (player.dailyFragmentBought >= 100) {
                Service.gI().sendThongBao(player, "Bạn đã mua đủ 100 mảnh hôm nay, vui lòng quay lại ngày mai!");
                return;
            }
            player.dailyFragmentBought++;
            player.lastTimeFragmentBought = System.currentTimeMillis();
            boolean hasOpt31 = item.itemOptions.stream()
                    .anyMatch(io -> io.optionTemplate.id == 31);
            if (!hasOpt31) {
                item.itemOptions.add(new Item.ItemOption(31, 1));
            }
        }
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);

        if (paidWithGoldBar) {
            GoldBarSpendService.gI().addPoint(player, is.cost);
        }

        if (itemTempId == 1523 || itemTempId == 1524 || itemTempId == 521) {
            updateAutoTrainPurchase(player, itemTempId);
            Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);
        }
    }

    private boolean hasMaxedOozarun1Card(Player player) {
        if (player.Cards == null) {
            return false;
        }
        Card oozarun1Card = player.Cards.stream()
                .filter(card -> card != null && card.Id == OOZARUN_1_CARD_ID)
                .findFirst()
                .orElse(null);
        return oozarun1Card != null && oozarun1Card.Level >= RADAR_CARD_MAX_LEVEL;
    }

    private boolean checkAutoTrainPurchase(Player player, int itemTempId) {
        int autoTrainState = player.autoTrainState;
        if (itemTempId == 1524 && autoTrainState != 2) {
            Service.gI().sendThongBao(player, "Bạn cần mua Tự động luyện tập 2 trước!");
            return false;
        } else if (itemTempId == 1523 && autoTrainState != 1) {  // sửa lại thành 'else if'
            Service.gI().sendThongBao(player, "Bạn cần mua Tự động luyện tập 1 trước!");
            return false;
        }

        return true;
    }

    private void updateAutoTrainPurchase(Player player, int itemTempId) {

        if (itemTempId == 1524) {
            player.autoTrainState = 0;

        } // Nếu mua Tự động luyện tập 1 (với ID 521)
        else if (itemTempId == 521) {
            // Nếu player đã có autoTrainState = 2, không thay đổi trạng thái nữa
            if (player.autoTrainState != 2) {
                player.autoTrainState = 1;
            }
        } else if (itemTempId == 1523) {
            player.autoTrainState = 2;
        }
    }

    private void buyDanhHieu(Player pl, ItemShop is) {
        int idEffect = BagesTemplate.fineIdEffectbyIdItem(is.temp.id);
        int percent = BadgesTaskService.sendPercenBadgesTask(pl, idEffect);

        for (BadgesData badge : pl.dataBadges) {
            if (badge.idBadGes == idEffect) {
                Service.gI().sendThongBao(pl, "Bạn đã sở hữu danh hiệu này rồi");
                return;
            }
        }

        boolean buyByGold = false;
        if (percent < 100) {
            if (pl.inventory.gold < BADGE_GOLD_COST) {
                Service.gI().sendThongBao(pl, "Bạn không đủ vàng, còn thiếu "
                        + Util.numberToMoney(BADGE_GOLD_COST - pl.inventory.gold) + " vàng");
                return;
            }
            pl.inventory.gold -= BADGE_GOLD_COST;
            Service.gI().sendMoney(pl);
            buyByGold = true;
        }

        new BadgesData(pl, idEffect, 30);

        BagesTemplate template = BagesTemplate.fineBadgesbyIdItem(is.temp.id);
        String badgeName = template != null ? template.NAME : "không rõ";

        Service.gI().sendThongBao(pl, (buyByGold ? "Bạn đã mua danh hiệu " : "Bạn đã nhận được danh hiệu ") + badgeName);
    }

    private void changeDanhHieu(Player pl, ItemShop is) {
        if (pl.lastTimeChangeBadges - System.currentTimeMillis() > 0) {
            Service.gI().sendThongBao(pl, "Vui lòng đợi " + (pl.lastTimeChangeBadges - System.currentTimeMillis()) / 1000 + " giây nữa");
            return;
        }
        if (pl.badges.idBadges == BagesTemplate.fineIdEffectbyIdItem(is.temp.id)) {
            Service.gI().sendThongBao(pl, "Danh hiệu đang được sữ dụng, hãy chọn danh hiệu khác");
            pl.lastTimeChangeBadges = System.currentTimeMillis() + 3000;
            return;
        }
        BadgesService.turnOnBadges(pl, BagesTemplate.fineIdEffectbyIdItem(is.temp.id));
        Service.gI().sendThongBao(pl, "Đã đổi danh hiệu sang " + is.temp.name);
        pl.lastTimeChangeBadges = System.currentTimeMillis() + 3000;
        pl.nPoint.calPoint();
        pl.nPoint.setHp(pl.nPoint.hpMax);
        pl.nPoint.setMp((int) pl.nPoint.mpMax);
        pl.nPoint.setDame(pl.nPoint.dame);
        Service.gI().point(pl);
        Service.gI().Send_Info_NV(pl);
    }

    private boolean isGoldBarCost(ItemShop itemShop) {
        if (itemShop == null) {
            return false;
        }
        short itSpec = itemShop.iconSpec == GOLD_BAR_ICON_ID
                ? GOLD_BAR_ITEM_ID : ItemService.gI().getItemIdByIcon((short) itemShop.iconSpec);
        return itSpec == GOLD_BAR_ITEM_ID;
    }

    private boolean isChiChiGemPackage(Shop shop, ItemShop itemShop) {
        return shop != null
                && SHOP_CHI_CHI.equals(shop.tagName)
                && itemShop != null
                && itemShop.id == CHI_CHI_GEM_PACKAGE_SHOP_ID;
    }

    private void buyChiChiGemPackage(Player player) {
        Item goldBar = InventoryService.gI().findItemBag(player, GOLD_BAR_ITEM_ID);
        int currentGoldBars = goldBar == null ? 0 : goldBar.quantity;
        if (currentGoldBars < CHI_CHI_GEM_PACKAGE_GOLD_BAR_COST) {
            Service.gI().sendThongBao(player, "Bạn không đủ Thỏi Vàng, còn thiếu "
                    + (CHI_CHI_GEM_PACKAGE_GOLD_BAR_COST - currentGoldBars) + " Thỏi Vàng.");
            return;
        }
        if ((long) player.inventory.gem + CHI_CHI_GEM_PACKAGE_AMOUNT > Integer.MAX_VALUE) {
            Service.gI().sendThongBao(player, "Số Ngọc sau khi mua sẽ vượt quá giới hạn nhân vật.");
            return;
        }

        InventoryService.gI().subQuantityItemsBag(player, goldBar, CHI_CHI_GEM_PACKAGE_GOLD_BAR_COST);
        player.inventory.gem += CHI_CHI_GEM_PACKAGE_AMOUNT;
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        GoldBarSpendService.gI().addPoint(player, CHI_CHI_GEM_PACKAGE_GOLD_BAR_COST);
        Service.gI().sendThongBao(player, "Mua thành công "
                + Util.numberToMoney(CHI_CHI_GEM_PACKAGE_AMOUNT) + " Ngọc với giá "
                + Util.numberToMoney(CHI_CHI_GEM_PACKAGE_GOLD_BAR_COST) + " Thỏi Vàng.");
    }

    private boolean subIemByItemShop(Player pl, ItemShop itemShop) {
        boolean isBuy = false;
        short itSpec = itemShop.iconSpec == GOLD_BAR_ICON_ID
                ? GOLD_BAR_ITEM_ID : ItemService.gI().getItemIdByIcon((short) itemShop.iconSpec);
        int buySpec = itemShop.cost;
        Item itS = ItemService.gI().createNewItem(itSpec);
        switch (itS.template.id) {
            case 76:
            case 188:
            case 189:
            case 190:
                if (pl.inventory.gold >= buySpec) {
                    pl.inventory.gold -= buySpec;
                    isBuy = true;
                } else {
                    Service.gI().sendThongBao(pl, "Bạn Không Đủ Vàng Để Mua Vật Phẩm");
                    isBuy = false;
                }
                break;
            case 77:
                if (pl.inventory.gem >= buySpec) {
                    pl.inventory.gem -= buySpec;
                    isBuy = true;
                } else {
                    Service.gI().sendThongBao(pl, "Bạn Không Đủ Ngọc Để Mua Vật Phẩm");
                    isBuy = false;
                }
                break;
            case ConstItem.HONG_NGOC:
                if (pl.inventory.ruby >= buySpec) {
                    pl.inventory.ruby -= buySpec;
                    isBuy = true;
                } else {
                    Service.gI().sendThongBao(pl, "Bạn Không Đủ Hồng Ngọc Để Mua Vật Phẩm");
                    isBuy = false;
                }
                break;
            default:
                if (InventoryService.gI().findItemBag(pl, itSpec) == null || !InventoryService.gI().findItemBag(pl, itSpec).isNotNullItem()) {
                    Service.gI().sendThongBao(pl, "Không tìm thấy " + itS.template.name);
                    isBuy = false;
                } else if (InventoryService.gI().findItemBag(pl, itSpec).quantity < buySpec) {
                    Service.gI().sendThongBao(pl, "Bạn không có đủ " + buySpec + " " + itS.template.name);
                    isBuy = false;
                } else {
                    InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, itSpec), buySpec);
                    isBuy = true;
                }
                break;
        }
        return isBuy;
    }

    public void showConfirmSellItem(Player pl, int where, int index) {
        Item item = null;
        if (where == 0) {
            if (index < 0 || index >= pl.inventory.itemsBody.size()) {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
                return;
            }
            item = pl.inventory.itemsBody.get(index);
        } else {
            if (pl.getSession().version < 220) {
                index -= (pl.inventory.itemsBody.size() - 7);
            }
            if (index < 0 || index >= pl.inventory.itemsBag.size()) {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
                return;
            }
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
                return;
            }
            int quantity = item.quantity;
            int cost = item.template.gold;
            if (item.template.id == 457) {
                if (quantity >= 1) {
                    Input.gI().createFormBanSLL(pl);
                    return;
                }
                quantity = 1;
            } else {
                cost /= 4;
            }
            if (cost == 0) {
                cost = 1;
            }
            cost *= quantity;

            String text = "Bạn có muốn bán\nx" + quantity
                    + " " + item.template.name + "\nvới giá là " + Util.numberToMoney(cost) + " vàng?";
            Message msg = null;
            try {
                msg = new Message(7);
                msg.writer().writeByte(where);
                msg.writer().writeShort(index);
                msg.writer().writeUTF(text);
                pl.sendMessage(msg);
            } catch (Exception e) {
            e.printStackTrace();
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public void sellItem(Player pl, int where, int index) {
        if (pl.idMark.getShopOpen() == null || pl.idMark.getTagNameShop() == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        if (index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        Item item = null;
        if (where == 0) {
            if (index >= pl.inventory.itemsBody.size()) {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
                return;
            }
            item = pl.inventory.itemsBody.get(index);
        } else {
            if (index >= pl.inventory.itemsBag.size()) {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
                return;
            }
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
                return;
            }
            if (item.template.id == 457) {
                Input.gI().createFormBanSLL(pl);
                return;
            }
            if (InventoryService.gI().getParam(pl, 93, item.template.id) > 0) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm có hạn sử dụng");
                return;
            }
            int quantity = item.quantity;
            int cost = item.template.gold;
            cost /= 4;
            if (cost == 0) {
                cost = 1;
            }
            cost *= quantity;

            if (pl.inventory.gold + cost > Inventory.LIMIT_GOLD) {
                Service.gI().sendThongBao(pl, "Vàng sau khi bán vượt quá giới hạn");
                return;
            }
            pl.inventory.gold += cost;
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Đã bán " + item.template.name
                    + " thu được " + Util.numberToMoney(cost) + " vàng");

            //Add vật phẩm đã bán
            BuyBackService.gI().addItem(pl, item);
            if (where == 0) {
                InventoryService.gI().subQuantityItemsBody(pl, item, quantity);
                InventoryService.gI().sendItemBody(pl);
                Service.gI().Send_Caitrang(pl);
            } else {
                InventoryService.gI().subQuantityItemsBag(pl, item, quantity);
                InventoryService.gI().sendItemBags(pl);
            }
            if ("BUNMA".equals(pl.idMark.getTagNameShop())
                    || "DENDE".equals(pl.idMark.getTagNameShop())
                    || "APPULE".equals(pl.idMark.getTagNameShop())) {
                AchievementService.gI().checkDoneTask(pl, ConstAchievement.TRUM_NHAT_VE_CHAI);
            }
        } else {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void getItemSideBoxLuckyRound(Player player, List<Item> items, byte type, int index) {
        if (items == null) {
            return;
        }
        if (index < 0 || index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = items.get(index);
        switch (type) {
            case 0: //nhận
                if (item.isNotNullItem()) {
                    if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                        InventoryService.gI().addItemBag(player, item);
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng" : item.template.name));
                        InventoryService.gI().sendItemBags(player);
                        items.remove(index);
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đã đầy");
                    }
                } else {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                }
                break;
            case 1: //xóa
                items.remove(index);
                Service.gI().sendThongBao(player, "Xóa vật phẩm thành công");
                break;
            case 2: //nhận hết
                for (int i = items.size() - 1; i >= 0; i--) {
                    item = items.get(i);
                    if (InventoryService.gI().addItemBag(player, item)) {
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng" : item.template.name));
                        items.remove(i);
                    }
                }
                InventoryService.gI().sendItemBags(player);
                break;
        }
        openShopType4(player, player.idMark.getTagNameShop(), items);
    }

    private void buyItemDaBan(Player player, List<Item> items, byte type, int index) {
        if (items == null) {
            return;
        }
        if (index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = items.get(index);
        int giamualaingoc = item.template.gem / 2;
        int giamualaivang = giamualaingoc == 0 ? (int) item.template.gold / 2 > 0 ? (int) item.template.gold / 2 : item.quantity * 100 : 0;
        if (giamualaivang > 0 && player.inventory.gold < giamualaivang) {
            Service.gI().sendThongBao(player, "Bạn không có đủ vàng!");
            return;
        }
        if (giamualaingoc > 0 && player.inventory.gem < giamualaingoc) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc xanh!");
            return;
        }
        player.inventory.gem -= giamualaingoc;
        player.inventory.gold -= giamualaivang;
        Service.gI().sendMoney(player);
        if (item.isNotNullItem()) {
            if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                InventoryService.gI().addItemBag(player, item);
                Service.gI().sendThongBao(player,
                        "Bạn nhận được " + (item.template.id == 189
                                ? Util.numberToMoney(item.quantity) + " vàng" : item.template.name));
                InventoryService.gI().sendItemBags(player);
                items.remove(index);
            } else {
                Service.gI().sendThongBao(player, "Hành trang đã đầy");
            }
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
        openShopType8(player, player.idMark.getTagNameShop(), items);
    }

    private void buyItemHD(Player player, int itemTempId) {
        Shop shop = player.idMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = ItemService.gI().createItemFromItemShop(is);
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy, không thể chứa thêm.");
            return;
        }
        if (!subMoneyByItemShopV2(player, is)) {
            return;
        }
        if (item.template.level == 14) {
            // Trừ 99 thức ăn
            Item doAn = player.inventory.itemsBag.stream().filter(it -> it != null && it.template != null
                    && (it.template.id == 663 || it.template.id == 664 || it.template.id == 665
                    || it.template.id == 666 || it.template.id == 667) && it.quantity >= 99).findFirst().orElse(null);
            if (doAn != null) {
                InventoryService.gI().subQuantityItemsBag(player, doAn, 99);
            } else {
                Service.gI().sendThongBao(player, "Cần 99 thức ăn để mua đồ Hủy Diệt!");
                return;
            }
            // Trừ 5 đồ Thần Linh từ body của sư phụ đang mặc (ID 555-567)
            List<Item> thanLinhItems = new ArrayList<>();
            for (Item it : player.inventory.itemsBody) {
                if (it != null && it.isNotNullItem() && it.template.id >= 555 && it.template.id <= 567) {
                    thanLinhItems.add(it);
                    if (thanLinhItems.size() >= 5) break;
                }
            }
            if (thanLinhItems.size() < 5) {
                Service.gI().sendThongBao(player, "Cần mặc đủ 5 trang bị Thần Linh (ID 555-567) trên người sư phụ!");
                return;
            }
            // Tháo ra khỏi body (set về null)
            for (Item it : thanLinhItems) {
                int idx = player.inventory.itemsBody.indexOf(it);
                if (idx >= 0) {
                    player.inventory.itemsBody.set(idx, ItemService.gI().createItemNull());
                }
            }
            player.nPoint.calPoint();
            InventoryService.gI().sendItemBags(player);
            InventoryService.gI().sendItemBody(player);
        }
        int param = 0;
        if (item.template.level == 14) {
            int random = Util.nextInt(1, 100);
            if (random <= 1) {
                param = 15;
            } else if (random <= 15) {
                param = Util.nextInt(11, 14);
            } else if (random <= 35) {
                param = Util.nextInt(7, 10);
            } else if (random <= 60) {
                param = Util.nextInt(4, 6);
            } else {
                param = Util.nextInt(0, 3);
            }
        }

        List<ItemOption> itemoptions = new ArrayList<>();
        if (!item.itemOptions.isEmpty()) {
            for (ItemOption ios : item.itemOptions) {
                if (item.template.level == 14 && InventoryService.gI().optionCanUpgrade(ios.optionTemplate.id) && param > 0) {
                    int id = ios.optionTemplate.id;
                    int param1 = ios.param + (ios.param * param) / 100;
                    itemoptions.add(new ItemOption(id, param1));
                } else if (ios.optionTemplate.id != 164) {
                    itemoptions.add(new ItemOption(ios.optionTemplate.id, ios.param));
                }
            }
        } else {
            itemoptions.add(new ItemOption(73, (short) 0));
        }
        itemoptions.add(new ItemOption(30, (short) 0));
        // Random 1 trong 3 option bonus (HP / SD / KI) xác suất đều nhau
        if (item.template.level == 14) {
            int roll = Util.nextInt(3); // 0, 1, 2 đều nhau
            switch (roll) {
                case 0 -> itemoptions.add(new ItemOption(77,  Util.nextInt(1, 5)));  // HP +1~5%
                case 1 -> itemoptions.add(new ItemOption(50,  Util.nextInt(1, 3)));  // SD +1~3%
                case 2 -> itemoptions.add(new ItemOption(103, Util.nextInt(1, 5)));  // KI +1~5%
            }
        }
        item.itemOptions.clear();
        item.itemOptions.addAll(itemoptions);
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);
    }

    private Item buyMagicPean(Player player, int[][] listDauThan, Item item) {
        for (int i = 0; i < listDauThan.length; i++) {
            if (item.template.id == listDauThan[i][1]) {
                item = ItemService.gI().createNewItem((short) listDauThan[i][0]);
                item.itemOptions.add(new Item.ItemOption(player.magicTree.level - 1 > 1 ? 2 : 48, MagicTree.PEA_PARAM[player.magicTree.level - 1]));
                item.quantity = 30;
                return item;
            }
        }
        return item;
    }

}
