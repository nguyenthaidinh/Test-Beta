package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.matches.dai_hoi_vo_thuat.DeathOrAliveArena;
import nro.models.matches.giai_dau.DeathOrAliveArenaManager;
import nro.models.matches.dai_hoi_vo_thuat.DeathOrAliveArenaService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.map.service.ChangeMapService;
import nro.models.combine.CombineService;
import nro.models.combine.CheTaoCuonSachCu;
import nro.models.combine.DoiSachTuyetKy;
import nro.models.combine.NangCapVatPham;
import nro.models.consts.ConstDailyGift;
import nro.models.daily_Giftcode.DailyGiftService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nro.models.shop.ShopService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class BaHatMit extends Npc {

    public BaHatMit(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 5 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi tìm ta có việc gì?",
                            "Chức năng\npha lê",
                            "Chuyển hóa\nTrang bị",
                            "Võ đài\nSinh tử",
                            "Phân rã\nTrang bị\nKích hoạt",
                            "Tái tạo\nCapsule\nKích hoạt",
                            "Hiến tế\nThần Linh\n→ Kích Hoạt",
                            "Hồn ma"
                    );

                case 112 -> {
                    if (Util.isAfterMidnight(player.lastTimePKVoDaiSinhTu)) {
                        player.haveRewardVDST = false;
                        player.thoiVangVoDaiSinhTu = 0;
                    }
                    if (player.haveRewardVDST) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Đây là phần thưởng cho con.",
                                "1 vệ tinh\nngẫu nhiên");
                        return;
                    }
                    if (DeathOrAliveArenaManager.gI().getVDST(player.zone) != null) {
                        if (DeathOrAliveArenaManager.gI().getVDST(player.zone).getPlayer().equals(player)) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Ngươi muốn hủy đăng ký thi đấu võ đài?",
                                    "Top 100", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
                            return;
                        }
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó",
                                "Top 100", "Bình chọn", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
                        return;
                    }
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó",
                            "Top 100", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
                }
                case 174 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi tìm ta có việc gì?",
                            "Quay về", "Từ chối");
                case 181 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi tìm ta có việc gì?",
                            "Quay về", "Từ chối");
                default -> {
                    List<String> menu = new ArrayList<>(Arrays.asList(
                            "Sách\nTuyệt Kỹ",
                            "Cửa hàng\nBùa",
                            "Nâng cấp\nVật phẩm",
                            "Bông tai\nPorata\nCấp 2 / 3",
                            "Làm phép\nNhập đá",
                            "Nhập\nNgọc Rồng",
                            "Phượng hoàng\nlửa"
                    ));

                    // Thêm thưởng bùa nếu còn lượt hôm nay (đặt lên đầu để case 0 vẫn là thưởng bùa)
                    if (DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_BUA_MIEN_PHI)) {
                        menu.add(0, "Thưởng\nBùa 1h\nngẫu nhiên");
                    }

                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?", menu.toArray(new String[0]));
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 5 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                createOtherMenu(player, 3,
                                        "Ta có thể giúp gì cho ngươi ?",
                                        "Ép sao\ntrang bị",
                                        "Pha lê\nhóa\ntrang bị",
                                        "Nâng cấp\nSao pha lê",
                                        "Đánh bóng\nSao pha lê",
                                        "Cường hóa\nlỗ sao\npha lê",
                                        "Tạo đá\nHematite");
                            case 1 ->
                                createOtherMenu(player, 4,
                                        "Ta có thể giúp gì cho ngươi ?",
                                        "Chuyển hóa\nVàng",
                                        "Chuyển hóa\nNgọc");
                            case 2 ->
                                ChangeMapService.gI().changeMapNonSpaceship(player, 112, 200 + Util.nextInt(-100, 100), 408);
                            case 3 ->
                                CombineService.gI().openTabCombine(player, CombineService.PHAN_RA_TRANG_BI_KH);
                            case 4 ->
                                CombineService.gI().openTabCombine(player, CombineService.TAI_TAO_CAPSULE_KH);
                            case 5 ->
                                CombineService.gI().openTabCombine(player, CombineService.HIEN_TE_THAN_LINH);
                            case 6 ->
                                createOtherMenu(player, ConstNpc.MENU_HON_MA,
                                        "Ta có thể giúp ngươi nâng Sói Địa Ngục và xử lý các Hồn ma.\nHãy chọn chức năng:",
                                        "Nâng cấp\nSói Địa Ngục",
                                        "Ép option\nHồn ma",
                                        "Đóng");
                        }
                    } else if (player.idMark.getIndexMenu() == 3) {
                        switch (select) {
                            case 0:
                                CombineService.gI().openTabCombine(player, CombineService.EP_SAO_TRANG_BI);
                                break;
                            case 1:
                                CombineService.gI().openTabCombine(player, CombineService.PHA_LE_HOA_TRANG_BI);
                                break;
                            case 2:
                                CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_SAO_PHA_LE);
                                break;
                            case 3:
                                CombineService.gI().openTabCombine(player, CombineService.DANH_BONG_SAO_PHA_LE);
                                break;
                            case 4:
                                CombineService.gI().openTabCombine(player, CombineService.CUONG_HOA_LO_SAO_PHA_LE);
                                break;
                            case 5:
                                CombineService.gI().openTabCombine(player, CombineService.TAO_DA_HEMATITE);
                                break;
                        }
                    } else if (player.idMark.getIndexMenu() == 4) {
                        switch (select) {
                            case 0:
                                CombineService.gI().openTabCombine(player, CombineService.CHUYEN_HOA_TRANG_BI_VANG);
                                break;
                            case 1:
                                CombineService.gI().openTabCombine(player, CombineService.CHUYEN_HOA_TRANG_BI_NGOC);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_HON_MA) {
                        switch (select) {
                            case 0 ->
                                CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_SOI_DIA_NGUC);
                            case 1 ->
                                CombineService.gI().openTabCombine(player, CombineService.EP_OPTION_SOI_DIA_NGUC);
                            default -> {
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                        switch (player.combineNew.typeCombine) {
                            case CombineService.EP_OPTION_SOI_DIA_NGUC, CombineService.NANG_CAP_SOI_DIA_NGUC -> {
                                if (select == 0) {
                                    CombineService.gI().startCombine(player);
                                }
                            }
                            case CombineService.EP_SAO_TRANG_BI, CombineService.PHA_LE_HOA_TRANG_BI, CombineService.CHUYEN_HOA_TRANG_BI_VANG, CombineService.CHUYEN_HOA_TRANG_BI_NGOC, CombineService.PHAN_RA_TRANG_BI_KH, CombineService.TAI_TAO_CAPSULE_KH, CombineService.HIEN_TE_THAN_LINH, CombineService.NANG_CAP_SAO_PHA_LE, CombineService.DANH_BONG_SAO_PHA_LE, CombineService.CUONG_HOA_LO_SAO_PHA_LE, CombineService.TAO_DA_HEMATITE, CombineService.NANG_CAP_VAT_PHAM, CombineService.NANG_CAP_BONG_TAI, CombineService.NANG_CHI_SO_BONG_TAI, CombineService.NANG_CAP_BONG_TAI3, CombineService.NANG_CHI_SO_BONG_TAI3, CombineService.NHAP_NGOC_RONG, CombineService.BAN_DO_THAN_LINH_LIO, CombineService.PHAN_RA_DO_THAN_LINH, CombineService.LAM_PHEP_NHAP_DA, CombineService.GIAM_DINH_SACH, CombineService.TAY_SACH, CombineService.NANG_CAP_SACH_TUYET_KY, CombineService.HOI_PHUC_SACH, CombineService.PHAN_RA_SACH -> {
                                switch (select) {
                                    case 0 ->
                                        CombineService.gI().startCombine(player);
                                    case 1 ->
                                        CombineService.gI().startCombineVip(player, 10);
                                    case 2 ->
                                        CombineService.gI().startCombineVip(player, 100);
                                    default -> {
                                    }
                                }
                            }
                        }
                    }
                }
                case 112 -> {
                    if (player.idMark.isBaseMenu()) {
                        if (player.haveRewardVDST) {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                        Item item = ItemService.gI().createNewItem((short) (Util.nextInt(342, 345)));
                                        item.itemOptions.add(new Item.ItemOption(93, 30));
                                        InventoryService.gI().addItemBag(player, item);
                                        InventoryService.gI().sendItemBags(player);
                                        Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name);
                                        player.haveRewardVDST = false;
                                    } else {
                                        Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống, không thể nhặt thêm");
                                    }
                                }
                            }
                            return;
                        }
                        if (DeathOrAliveArenaManager.gI().getVDST(player.zone) != null) {
                            if (DeathOrAliveArenaManager.gI().getVDST(player.zone).getPlayer().equals(player)) {
                                switch (select) {
                                    case 0 -> {
                                    }
                                    case 1 ->
                                        this.npcChat("Không thể thực hiện");
                                    case 2 -> {
                                    }
                                    case 3 ->
                                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                                }
                                return;
                            }
                            switch (select) {
                                case 0 -> {
                                }
                                case 1 ->
                                    this.createOtherMenu(player, ConstNpc.DAT_CUOC_HAT_MIT,
                                            "Phí bình chọn là 1 triệu vàng\nkhi trận đấu kết thúc\n90% tổng tiền bình chọn sẽ chia đều cho phe bình chọn chính xác",
                                            "Bình chọn cho " + DeathOrAliveArenaManager.gI().getVDST(player.zone).getPlayer().name + " (" + DeathOrAliveArenaManager.gI().getVDST(player.zone).getCuocPlayer() + ")",
                                            "Bình chọn cho hạt mít (" + DeathOrAliveArenaManager.gI().getVDST(player.zone).getCuocBaHatMit() + ")");
                                case 2 ->
                                    DeathOrAliveArenaService.gI().startChallenge(player);
                                case 3 -> {
                                }
                                case 4 ->
                                    ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                            }
                            return;
                        }
                        switch (select) {
                            case 0 -> {
                            }
                            case 1 ->
                                DeathOrAliveArenaService.gI().startChallenge(player);
                            case 2 -> {
                            }
                            case 3 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.DAT_CUOC_HAT_MIT) {
                        if (DeathOrAliveArenaManager.gI().getVDST(player.zone) != null) {
                            switch (select) {
                                case 0 -> {
                                    if (player.inventory.gold >= 1_000_000) {
                                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                                        vdst.setCuocPlayer(vdst.getCuocPlayer() + 1);
                                        vdst.addBinhChon(player);
                                        player.binhChonPlayer++;
                                        player.zoneBinhChon = player.zone;
                                        player.inventory.gold -= 1_000_000;
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(1_000_000 - player.inventory.gold) + " vàng nữa");
                                    }
                                }
                                case 1 -> {
                                    if (player.inventory.gold >= 1_000_000) {
                                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                                        vdst.setCuocBaHatMit(vdst.getCuocBaHatMit() + 1);
                                        vdst.addBinhChon(player);
                                        player.binhChonHatMit++;
                                        player.zoneBinhChon = player.zone;
                                        player.inventory.gold -= 1_000_000;
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(1_000_000 - player.inventory.gold) + " vàng nữa");
                                    }
                                }
                            }
                        }
                    }
                }
                case 174 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    }
                }
                case 181 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    }
                }
                case 42, 43, 44, 84 -> {
                    if (player.idMark.isBaseMenu()) {
                        if (!DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_BUA_MIEN_PHI)) {
                            select++;
                        }
                        switch (select) {
                            case 0:
                                if (DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_BUA_MIEN_PHI)) {
                                    int idItem = Util.nextInt(213, 219);
                                    player.charms.addTimeCharms(idItem, 60);
                                    Item bua = ItemService.gI().createNewItem((short) idItem);
                                    Service.gI().sendThongBao(player, "Bạn vừa nhận thưởng " + bua.template.name);
                                    DailyGiftService.updateDailyGift(player, ConstDailyGift.NHAN_BUA_MIEN_PHI);
                                } else {
                                    Service.gI().sendThongBao(player, "Hôm nay bạn đã nhận bùa miễn phí rồi!!!");
                                }
                                break;
                            case 1:
                                createOtherMenu(player, ConstNpc.MENU_SACH_TUYET_KY, "Ta có thể giúp gì cho ngươi ?",
                                        "Đóng thành\nSách cũ",
                                        "Đổi Sách\nTuyệt kỹ",
                                        "Giám định\nSách",
                                        "Tẩy\nSách",
                                        "Nâng cấp\nSách\nTuyệt kỹ",
                                        "Hồi phục\nSách",
                                        "Phân rã\nSách");
                                break;
                            case 2:
                                createOtherMenu(player, ConstNpc.MENU_OPTION_SHOP_BUA, "Bùa của ta rất lợi hại, nhìn ngươi yếu đuối thế này, chắc muốn mua bùa để " + "mạnh mẽ à, mua không ta bán cho, xài rồi lại thích cho mà xem.",
                                        "Bùa\n1 giờ",
                                        "Bùa\n8 giờ",
                                        "Bùa\n1 tháng", "Đóng");
                                break;
                            case 3:
                                CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_VAT_PHAM);
                                break;
                            case 4:
                                createOtherMenu(player, ConstNpc.MENU_BONG_TAI_OPTIONS,
                                        "Bông tai Porata - chọn cấp muốn xử lý:",
                                        "Bông tai\nCấp 2",
                                        "Bông tai\nCấp 3",
                                        "Đổi mảnh\nBông tai Cấp 3",
                                        "Đóng");
                                break;
                            case 5:
                                CombineService.gI().openTabCombine(player, CombineService.LAM_PHEP_NHAP_DA);
                                break;
                            case 6:
                                CombineService.gI().openTabCombine(player, CombineService.NHAP_NGOC_RONG);
                                break;
                            case 7:
                                createOtherMenu(player, ConstNpc.MENU_PHUONG_HOANG_LUA,
                                        "Ta có thể giúp ngươi nâng cấp hoặc tẩy chỉ số Phượng hoàng lửa.",
                                        "Nâng cấp",
                                        "Tẩy chỉ số",
                                        "Đóng");
                                break;
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_PHUONG_HOANG_LUA) {
                        switch (select) {
                            case 0 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_PHUONG_HOANG_LUA);
                            case 1 -> CombineService.gI().openTabCombine(player, CombineService.TAY_CHI_SO_PHUONG_HOANG_LUA);
                            default -> {
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_SACH_TUYET_KY) {
                        switch (select) {
                            case 0:
                                CheTaoCuonSachCu.showCombine(player);
                                break;
                            case 1:
                                DoiSachTuyetKy.showCombine(player);
                                break;
                            case 2:
                                CombineService.gI().openTabCombine(player, CombineService.GIAM_DINH_SACH);
                                break;
                            case 3:
                                CombineService.gI().openTabCombine(player, CombineService.TAY_SACH);
                                break;
                            case 4:
                                CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_SACH_TUYET_KY);
                                break;
                            case 5:
                                CombineService.gI().openTabCombine(player, CombineService.HOI_PHUC_SACH);
                                break;
                            case 6:
                                CombineService.gI().openTabCombine(player, CombineService.PHAN_RA_SACH);
                                break;
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.DONG_THANH_SACH_CU) {
                        CheTaoCuonSachCu.cheTaoCuonSachCu(player);
                    } else if (player.idMark.getIndexMenu() == ConstNpc.DOI_SACH_TUYET_KY) {
                        if (select == 0) {
                            DoiSachTuyetKy.doiSachTuyetKy(player, false);
                        } else if (select == 1 && InventoryService.gI().findItemBag(player, 1794) != null) {
                            DoiSachTuyetKy.doiSachTuyetKy(player, true);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_SHOP_BUA) {
                        switch (select) {
                            case 0 ->
                                ShopService.gI().opendShop(player, "BUA_1H", true);
                            case 1 ->
                                ShopService.gI().opendShop(player, "BUA_8H", true);
                            case 2 ->
                                ShopService.gI().opendShop(player, "BUA_1M", true);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_BONG_TAI_OPTIONS) {
                        switch (select) {
                            case 0 -> createOtherMenu(player, ConstNpc.MENU_BT2_ACTIONS,
                                    "Bông tai Porata cấp 2 - chọn chức năng:",
                                    "Nâng Cấp 1\nlên Cấp 2",
                                    "Mở / đổi\nchỉ số Cấp 2",
                                    "Đóng");
                            case 1 -> createOtherMenu(player, ConstNpc.MENU_BT3_ACTIONS,
                                    "Bông tai Porata cấp 3 - chọn chức năng:",
                                    "Nâng Cấp 2\nlên Cấp 3",
                                    "Mở / đổi\nchỉ số Cấp 3",
                                    "Đóng");
                            case 2 -> openExchangeBt3Fragments(player);
                            default -> {
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_BT2_ACTIONS) {
                        switch (select) {
                            case 0 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_BONG_TAI);
                            case 1 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CHI_SO_BONG_TAI);
                            default -> {
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_BT3_ACTIONS) {
                        switch (select) {
                            case 0 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_BONG_TAI3);
                            case 1 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CHI_SO_BONG_TAI3);
                            default -> {
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_BT3_DOI_MANH) {
                        switch (select) {
                            case 0 -> exchangeBt3Fragments(player, false);
                            case 1 -> exchangeBt3Fragments(player, true);
                            // case 2: Hủy - không làm gì
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                        switch (player.combineNew.typeCombine) {
                            case CombineService.EP_SAO_TRANG_BI, CombineService.PHA_LE_HOA_TRANG_BI, CombineService.CHUYEN_HOA_TRANG_BI_VANG, CombineService.CHUYEN_HOA_TRANG_BI_NGOC, CombineService.PHAN_RA_TRANG_BI_KH, CombineService.TAI_TAO_CAPSULE_KH, CombineService.HIEN_TE_THAN_LINH, CombineService.NANG_CAP_SAO_PHA_LE, CombineService.DANH_BONG_SAO_PHA_LE, CombineService.CUONG_HOA_LO_SAO_PHA_LE, CombineService.TAO_DA_HEMATITE, CombineService.NANG_CAP_VAT_PHAM, CombineService.NANG_CAP_BONG_TAI, CombineService.NANG_CHI_SO_BONG_TAI, CombineService.NANG_CAP_BONG_TAI3, CombineService.NANG_CHI_SO_BONG_TAI3, CombineService.NHAP_NGOC_RONG, CombineService.BAN_DO_THAN_LINH_LIO, CombineService.PHAN_RA_DO_THAN_LINH, CombineService.LAM_PHEP_NHAP_DA, CombineService.GIAM_DINH_SACH, CombineService.TAY_SACH, CombineService.NANG_CAP_SACH_TUYET_KY, CombineService.HOI_PHUC_SACH, CombineService.PHAN_RA_SACH, CombineService.NANG_CAP_PHUONG_HOANG_LUA, CombineService.TAY_CHI_SO_PHUONG_HOANG_LUA -> {
                                switch (select) {
                                    case 0 ->
                                        CombineService.gI().startCombine(player);
                                    case 1 ->
                                        CombineService.gI().startCombineVip(player, 10);
                                    case 2 ->
                                        CombineService.gI().startCombineVip(player, 100);
                                    default -> {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void openExchangeBt3Fragments(Player player) {
        int manhBt2 = InventoryService.gI().getParam(player, 31, 933);
        if (manhBt2 < 10) {
            Service.gI().sendThongBao(player,
                    "Cần ít nhất 10 mảnh vỡ BT2! Bạn đang có: " + manhBt2);
            return;
        }
        int coTheDoi = manhBt2 / 10;
        createOtherMenu(player, ConstNpc.MENU_BT3_DOI_MANH,
                "|2|Mảnh vỡ BT2 hiện có: " + manhBt2 + "\n"
                + "|2|Có thể đổi tối đa: " + coTheDoi + " mảnh BT3\n"
                + "(10 mảnh BT2 = 1 mảnh BT3)",
                "Đổi 1 mảnh",
                "Đổi tất cả (" + coTheDoi + ")",
                "Hủy");
    }

    /**
     * Thêm mảnh vỡ BT3 (ID 1820, option 31) vào túi người chơi.
     * Nếu đã có item 1820, cộng thêm vào param 31; nếu chưa có thì tạo mới.
     */
    private void exchangeBt3Fragments(Player player, boolean exchangeAll) {
        synchronized (player) {
            int manhBt2 = InventoryService.gI().getParam(player, 31, 933);
            int amount = exchangeAll ? manhBt2 / 10 : 1;
            if (manhBt2 < 10 || amount < 1) {
                Service.gI().sendThongBao(player, "Không đủ mảnh BT2!");
                return;
            }

            Item manhBt3 = InventoryService.gI().findItemBag(player, 1820);
            if ((manhBt3 == null || !manhBt3.isNotNullItem())
                    && InventoryService.gI().getCountEmptyBag(player) < 1) {
                Service.gI().sendThongBao(player,
                        "Hành trang đầy, cần trống 1 ô để nhận mảnh BT3.");
                return;
            }
            if (!addManhBT3(player, amount)) {
                Service.gI().sendThongBao(player, "Không thể nhận mảnh BT3, vui lòng thử lại.");
                return;
            }

            int usedBt2 = amount * 10;
            InventoryService.gI().subParamItemsBag(player, 933, 31, usedBt2);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player,
                    "Đã đổi " + usedBt2 + " mảnh BT2 → " + amount + " mảnh BT3!");
        }
    }

    private boolean addManhBT3(Player player, int amount) {
        Item manh = InventoryService.gI().findItemBag(player, 1820);
        if (manh != null && manh.isNotNullItem()) {
            for (Item.ItemOption io : manh.itemOptions) {
                if (io.optionTemplate.id == 31) {
                    io.param += amount;
                    return true;
                }
            }
            // Không có option 31 → thêm mới
            manh.itemOptions.add(new Item.ItemOption(31, amount));
            return true;
        } else {
            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                return false;
            }
            Item newManh = ItemService.gI().createNewItem((short) 1820);
            newManh.itemOptions.add(new Item.ItemOption(31, amount));
            return InventoryService.gI().addItemBag(player, newManh);
        }
    }
}
