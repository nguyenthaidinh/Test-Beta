package nro.models.services;

import nro.models.consts.ConstPlayer;
import nro.models.boss.Broly.Broly;
import nro.models.boss.Broly.SuperBroly;
import nro.models.boss.sieu_hang.Rival;
import nro.models.boss.yardrat.Yardart;
import nro.models.consts.ConstAchievement;
import nro.models.consts.ConstItem;
import nro.models.intrinsic.Intrinsic;
import nro.models.item.Item;
import nro.models.mob.Mob;
import nro.models.mob.MobMe;
import nro.models.mob_bigboss.GauTuongCuop;
import nro.models.player.NPoint;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.player.NewSkill;
import nro.models.network.Message;
import java.io.IOException;
import nro.models.map.service.MapService;
import nro.models.utils.Logger;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.npc.NonInteractiveNPC;
import nro.models.services_func.EffectMapService;
import nro.models.skill.Skill;

/**
 *
 * @author By Mr Blue
 *
 */
public class SkillService {

    private static final int JACKY_CHUN_CHUONG_DELAY = 60_000;
    private static final int JACKY_CHUN_CHUONG_MULTIPLIER = 4;
    private static final long HIGH_DAMAGE_ANNOUNCE_THRESHOLD = 300_000_000L;
    private static final long HIGH_DAMAGE_ANNOUNCE_COOLDOWN = 5_000L;
    private final Map<Long, Long> lastHighDamageAnnouncements = new ConcurrentHashMap<>();

    private static SkillService instance;

    public static SkillService gI() {
        if (instance == null) {
            instance = new SkillService();
        }
        return instance;
    }

    public boolean useSkill(Player player, Player plTarget, Mob mobTarget, int status, Message msg) {
        if (plTarget != null && player.clan != null && plTarget.clan != null && player.clan == plTarget.clan && MapService.gI().isMapBlackBallWar(plTarget.zone.map.mapId)) {
            Service.gI().chatJustForMe(player, plTarget, "Ê cùng bang mà");
            return false;
        }
        if (plTarget != null && (player.idNRNM != -1 || plTarget.idNRNM != -1) && player.clan != null && plTarget.clan != null && player.clan == plTarget.clan) {
            Service.gI().chatJustForMe(player, plTarget, "Ê cùng bang mà");
            return false;
        }
        if (plTarget != null && !Util.canDoWithTime(plTarget.lastTimeRevived, 1500)) {
            return false;
        }

        byte skillId = -1;
        Short dx = -1;
        Short dy = -1;
        byte dir = -1;
        Short x = -1;
        Short y = -1;
        if (status == 20) {
            try {
                skillId = msg.reader().readByte();
                dx = msg.reader().readShort();
                dy = msg.reader().readShort();
                dir = msg.reader().readByte();
                x = msg.reader().readShort();
                y = msg.reader().readShort();
            } catch (IOException e) {
            }
        }
        if (player.playerSkill == null || player.playerSkill.skillSelect == null || player.playerSkill.skillSelect.template == null) {
            return false;
        }
        if (player.effectSkill != null && player.effectSkill.isHaveEffectSkill()
                && player.playerSkill.skillSelect.template.id != Skill.TU_SAT
                && player.playerSkill.skillSelect.template.id != Skill.QUA_CAU_KENH_KHI
                && player.playerSkill.skillSelect.template.id != Skill.MAKANKOSAPPO) {
            return false;
        }
        if (player.playerSkill.skillSelect.template.type == 2 && canUseSkillWithMana(player) && canUseSkillWithCooldown(player)) {
            useSkillBuffToPlayer(player, plTarget);
            return true;
        }
        if ((plTarget != null && !canAttackPlayer(player, plTarget))
                || (mobTarget != null && mobTarget.isDie())
                || !canUseSkillWithMana(player) || !canUseSkillWithCooldown(player)) {
            return false;
        }
        if (player.effectSkill != null && player.effectSkill.isHaveEffectSkill() && player.effectSkill.useTroi) {
            EffectSkillService.gI().removeUseTroi(player);
        }
        if (player.effectSkill != null && player.effectSkill.isCharging) {
            EffectSkillService.gI().stopCharge(player);
        }
        if (status == 20 && skillId != -1 && player.playerSkill.skillSelect.template.id != skillId) {
            selectSkill(player, skillId);
            return false;
        } else {
            switch (player.playerSkill.skillSelect.template.type) {
                case 1 ->
                    useSkillAttack(player, plTarget, mobTarget);
                case 3 ->
                    useSkillAlone(player);
                case 4 ->
                    useNewSkillNotFocus(player, plTarget, mobTarget, status, skillId, dx, dy, dir, x, y);
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    private void useNewSkillNotFocus(Player player, Player plTarget, Mob mobTarget, int status, byte skillId, Short dx, Short dy, byte dir, Short x, Short y) {
        try {
            if (skillId == -1 && (plTarget != null || mobTarget != null)) {
                skillId = player.playerSkill.skillSelect.template.id;
                dx = (short) player.location.x;
                dy = (short) player.location.y;
                if (plTarget != null) {
                    x = (short) plTarget.location.x;
                    y = (short) plTarget.location.y;
                } else {
                    x = (short) mobTarget.location.x;
                    y = (short) mobTarget.location.y;
                }
                dir = (byte) (dx > x ? -1 : 1);
            }
            switch (skillId) {
                case Skill.SUPER_KAME, Skill.LIEN_HOAN_CHUONG, Skill.MA_PHONG_BA -> {
                    player.newSkill.setSkillSpecial(dir, dx, dy, x, y);
                    newSkillNotFocus(player, status);
                    AchievementService.gI().checkDoneTask(player, ConstAchievement.TUYET_KY_THANH_THAO);
                }
            }
            affterUseSkill(player, player.playerSkill.skillSelect.template.id);
        } catch (Exception e) {
        }
    }

    public void updateSkillSpecial(Player player) {
        try {
            if (player.newSkill == null || player.zone == null) {
                return;
            }
            if (player.isDie() || player.effectSkill.isHaveEffectSkill()) {
                player.newSkill.closeSkillSpecial();
                return;
            }
            if (player.newSkill.skillSelect.template.id == Skill.MA_PHONG_BA) {
                if (Util.canDoWithTime(player.newSkill.lastTimeSkillSpecial, NewSkill.TIME_GONG)) {
                    if (Util.isTrue(1, 50) && player.isPl()) {
                        Service.gI().sendThongBao(player, "Bạn đã kiệt sức vì dùng ma phong ba quá nhiều!");
                        player.setDie();
                    }
                    player.newSkill.lastTimeSkillSpecial = System.currentTimeMillis();
                    player.newSkill.closeSkillSpecial();
                    List<Player> playersMap;
                    if (player.isBoss) {
                        playersMap = player.zone.getNotBosses();
                    } else {
                        playersMap = player.zone.getHumanoids();
                    }

                    for (Player playerMap : playersMap) {
                        if (playerMap == null || playerMap.id == player.id) {
                            continue;
                        }
                        if (player.newSkill.dir == -1 && !playerMap.isDie() && Util.getDistance(player, playerMap) <= 500 && this.canAttackPlayer(player, playerMap)) {
                            player.newSkill.playersTaget.add(playerMap);

                        } else if (player.newSkill.dir == 1 && !playerMap.isDie() && Util.getDistance(player, playerMap) <= 500 && this.canAttackPlayer(player, playerMap)) {
                            player.newSkill.playersTaget.add(playerMap);
                        }
                    }

                    if (!player.isBoss) {
                        for (Mob mobMap : player.zone.mobs) {
                            if (mobMap == null) {
                                continue;
                            }
                            if (player.newSkill.dir == -1 && !mobMap.isDie() && Util.getDistance(player, mobMap) <= 500) {
                                player.newSkill.mobsTaget.add(mobMap);
                                mobMap.addTemporaryEnemies(player);
                            } else if (player.newSkill.dir == 1 && !mobMap.isDie() && Util.getDistance(player, mobMap) <= 500) {
                                player.newSkill.mobsTaget.add(mobMap);
                                mobMap.addTemporaryEnemies(player);
                            }
                        }
                    }
                    newSkillNotFocus(player, 21);
                    EffectSkillService.gI().startUseMafuba(player, 4000);
                }
            } else {
                if (player.newSkill.stepSkillSpecial == 0 && Util.canDoWithTime(player.newSkill.lastTimeSkillSpecial, NewSkill.TIME_GONG)) {
                    player.newSkill.lastTimeSkillSpecial = System.currentTimeMillis();
                    player.newSkill.stepSkillSpecial = 1;
                    newSkillNotFocus(player, 21);
                } else if (player.newSkill.stepSkillSpecial == 1 && !Util.canDoWithTime(player.newSkill.lastTimeSkillSpecial, NewSkill.TIME_GONG)) {
                    List<Player> playersMap;
                    if (player.isBoss) {
                        playersMap = player.zone.getNotBosses();
                    } else {
                        playersMap = player.zone.getHumanoids();
                    }

                    for (Player playerMap : playersMap) {
                        if (playerMap == null || playerMap.id == player.id) {
                            continue;
                        }
                        if (player.newSkill.dir == -1 && player.location.x > playerMap.location.x && !playerMap.isDie()
                                && Math.abs(playerMap.location.x - player.newSkill._xPlayer) <= player.newSkill._xObjTaget
                                && Math.abs(playerMap.location.y - player.newSkill._yPlayer) <= player.newSkill._yObjTaget
                                && this.canAttackPlayer(player, playerMap)) {
                            this.playerAttackPlayer(player, playerMap, false);
                        }
                        if (player.newSkill.dir == 1 && player.location.x < playerMap.location.x && !playerMap.isDie()
                                && Math.abs(playerMap.location.x - player.newSkill._xPlayer) <= player.newSkill._xObjTaget
                                && Math.abs(playerMap.location.y - player.newSkill._yPlayer) <= player.newSkill._yObjTaget
                                && this.canAttackPlayer(player, playerMap)) {
                            this.playerAttackPlayer(player, playerMap, false);
                        }
                    }
                    if (!player.isBoss) {
                        for (Mob mobMap : player.zone.mobs) {
                            if (mobMap == null) {
                                continue;
                            }
                            if (player.newSkill.dir == -1 && player.location.x > mobMap.location.x && !mobMap.isDie()
                                    && Math.abs(mobMap.location.x - player.newSkill._xPlayer) <= player.newSkill._xObjTaget
                                    && Math.abs(mobMap.location.y - player.newSkill._yPlayer) <= player.newSkill._yObjTaget) {
                                this.playerAttackMob(player, mobMap, false, false);
                            }
                            if (player.newSkill.dir == 1 && player.location.x < mobMap.location.x && !mobMap.isDie()
                                    && Math.abs(mobMap.location.x - player.newSkill._xPlayer) <= player.newSkill._xObjTaget
                                    && Math.abs(mobMap.location.y - player.newSkill._yPlayer) <= player.newSkill._yObjTaget) {
                                this.playerAttackMob(player, mobMap, false, false);
                            }
                        }
                    }
                } else if (player.newSkill.stepSkillSpecial == 1) {
                    player.newSkill.closeSkillSpecial();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCurrLevelSpecial(Player player, Skill skill) {
        Message message = null;
        try {
            message = Service.gI().messageSubCommand((byte) 62);
            message.writer().writeShort(skill.skillId);
            message.writer().writeByte(0);
            message.writer().writeShort(skill.currLevel);
            player.sendMessage(message);
        } catch (final IOException ex) {
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
    }

    // _______________________________NEW_SKILL_NOT_FOCUS_______________________________
    public void newSkillNotFocus(Player player, int status) {
        Message msg = null;
        try {
            NewSkill newSkill = player.newSkill;
            msg = new Message(-45);
            msg.writer().writeByte(status);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(newSkill.skillSelect.template.id);
            if (status == 20) {
                byte typeFrame = 4;
                switch (newSkill.skillSelect.template.id) {
                    case Skill.SUPER_KAME ->
                        typeFrame = 1;
                    case Skill.LIEN_HOAN_CHUONG ->
                        typeFrame = 2;
                    case Skill.MA_PHONG_BA ->
                        typeFrame = 3;
                }
                byte dir = newSkill.dir;
                short timeGong = NewSkill.TIME_GONG;
                boolean isFly = false;
                byte typePaint = newSkill.typePaint;
                byte typeItem = newSkill.typeItem;
                msg.writer().writeByte(typeFrame);
                msg.writer().writeByte(dir);
                msg.writer().writeShort(timeGong);
                msg.writer().writeByte((byte) (isFly ? 1 : 0));
                msg.writer().writeByte(typePaint);
                msg.writer().writeByte(typeItem);
            } else if (status == 21) {
                short pointX = (short) (newSkill._xPlayer + ((newSkill.dir == -1) ? (-newSkill._xObjTaget) : newSkill._xObjTaget));
                short pointY = (short) newSkill._yPlayer;
                short timeDame = NewSkill.TIME_GONG;
                short rangeDame = newSkill._yObjTaget;
                byte typePaint = newSkill.getTypePaint();
                byte typeItem = newSkill.getTypeItem();
                byte num = (byte) (player.newSkill.playersTaget.size() + player.newSkill.mobsTaget.size());
                msg.writer().writeShort(pointX);
                msg.writer().writeShort(pointY);
                msg.writer().writeShort(timeDame);
                msg.writer().writeShort(rangeDame);
                msg.writer().writeByte(typePaint);
                msg.writer().writeByte(num);
                if (num > 0) {
                    for (Player playerMap : player.newSkill.playersTaget) {
                        msg.writer().writeByte(1);
                        msg.writer().writeInt((int) playerMap.id);
                    }
                    for (Mob mobMap : player.newSkill.mobsTaget) {
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(mobMap.id);
                    }
                }
                msg.writer().writeByte(typeItem);
            }
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void learSkillSpecial(Player player, byte skillID) {
        Message message = null;
        try {
            Skill curSkill = SkillUtil.createSkill(skillID, 1);
            SkillUtil.setSkill(player, curSkill);
            message = Service.gI().messageSubCommand((byte) 23);
            message.writer().writeShort(curSkill.skillId);
            player.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (message != null) {
                message.cleanup();
                message = null;
            }

        }
    }

    public void useSkillAttack(Player player, Player plTarget, Mob mobTarget) {
        if (player.effectSkill != null && player.effectSkill.useTroi) {
            EffectSkillService.gI().removeUseTroi(player);
        }
        if (!player.isBoss) {
            if (player.isPet) {
                if (player.nPoint.stamina > 0) {
                    player.nPoint.numAttack++;
                    boolean haveCharmPet = ((Pet) player).master.charms != null && ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis();
                    if (haveCharmPet ? player.nPoint.numAttack >= 5 : player.nPoint.numAttack >= 2) {
                        player.nPoint.numAttack = 0;
                        player.nPoint.stamina--;
                    }
                } else {
                    ((Pet) player).askPea();
                    return;
                }
            } else {
                if (player.nPoint.stamina > 0) {
                    if (player.charms.tdDeoDai < System.currentTimeMillis()) {
                        player.nPoint.numAttack++;
                        if (player.nPoint.numAttack == 500) {
                            player.nPoint.numAttack = 0;
                            player.nPoint.stamina--;
                            PlayerService.gI().sendCurrentStamina(player);
                        }
                    }
                } else {
                    Service.gI().sendThongBao(player, "Thể lực đã cạn kiệt, hãy nghỉ ngơi để lấy lại sức");
                    return;
                }
            }
        }
        List<Mob> mobs;
        boolean miss = false;
        if (player.playerSkill.skillSelect.template.id == Skill.KAMEJOKO || player.playerSkill.skillSelect.template.id == Skill.MASENKO || player.playerSkill.skillSelect.template.id == Skill.ANTOMIC) {
            if (!player.isBoss && !player.isBot && !player.isPet) {
            }
        }
        switch (player.playerSkill.skillSelect.template.id) {
            case Skill.KAIOKEN:
                long hpUse = player.nPoint.hpMax / 100 * 5; // Giảm 5% HP
                int kiUse = 10000; // Giảm 10k KI mỗi đấm
                if (player.setClothes.thanVuTruKaio == 4) {
                    hpUse = player.nPoint.hpMax / 100 * 2; // Set 4 món: 2% HP
                } else if (player.setClothes.thanVuTruKaio == 5) {
                    hpUse = 0; // Full set: miễn phí HP
                }
                if (player.nPoint.hp <= hpUse || player.nPoint.mp < kiUse) {
                    break;
                } else {
                    Service.gI().sendEffAllPlayer(player, 1027, 1, -1, 20);
                    player.nPoint.setHp(player.nPoint.hp - hpUse);
                    player.nPoint.setMP(player.nPoint.mp - kiUse);
                    PlayerService.gI().sendInfoHpMpMoney(player);
                    Service.gI().Send_Info_NV(player);
                }
            case Skill.DRAGON:
            case Skill.DEMON:
            case Skill.GALICK:
            case Skill.LIEN_HOAN:
                if (isMeleeAttackOutOfRange(player, plTarget)) {
                    miss = true;
                }
                if (mobTarget != null && Util.getDistance(player, mobTarget) > Skill.RANGE_ATTACK_CHIEU_DAM) {
                    miss = true;
                }
            case Skill.KAMEJOKO:
            case Skill.MASENKO:
            case Skill.ANTOMIC:
                if (plTarget != null) {
                    playerAttackPlayer(player, plTarget, miss);
                }
                if (mobTarget != null) {
                    playerAttackMob(player, mobTarget, miss, false);
                }
                if (player.mobMe != null) {
                    player.mobMe.attack(plTarget, mobTarget, miss);
                }
                if (player.playerSkill != null
                        && player.playerSkill.skillSelect != null
                        && player.playerSkill.skillSelect.template != null) {
                    affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                }
                break;
            //******************************************************************
            case Skill.QUA_CAU_KENH_KHI:
                if (!player.playerSkill.prepareQCKK) {
                    //bắt đầu tụ quả cầu
                    player.playerSkill.prepareQCKK = true;
                    player.playerSkill.lastTimePrepareQCKK = System.currentTimeMillis();
                    sendPlayerPrepareSkill(player, 4000);
                } else {
                    //ném cầu
                    player.playerSkill.prepareQCKK = false;
                    mobs = new ArrayList<>();
                    if (plTarget != null) {
                        playerAttackPlayer(player, plTarget, false);
                        if (!player.isBoss) {
                            for (Mob mob : player.zone.mobs) {
                                if (!mob.isDie()
                                        && Util.getDistance(plTarget, mob) <= SkillUtil.getRangeQCKK(player.playerSkill.skillSelect.point)) {
                                    mobs.add(mob);
                                }
                            }
                        }
                    }
                    if (mobTarget != null) {
                        if (!player.isBoss) {
                            playerAttackMob(player, mobTarget, false, true);
                            for (Mob mob : player.zone.mobs) {
                                if (!mob.equals(mobTarget) && !mob.isDie()
                                        && Util.getDistance(mob, mobTarget) <= SkillUtil.getRangeQCKK(player.playerSkill.skillSelect.point)) {
                                    mobs.add(mob);
                                }
                            }
                        }
                    }
                    for (Mob mob : mobs) {
                        long hpBefore = mob.point.gethp();
                        mob.injured(player, player.nPoint.getDameAttack(true), true);
                        announceHighDamage(player, Math.max(0, hpBefore - mob.point.gethp()));
                    }
                    PlayerService.gI().sendInfoHpMpMoney(player);
                    affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                }
                break;
            case Skill.MAKANKOSAPPO:
                if (!player.playerSkill.prepareLaze) {
                    //bắt đầu nạp laze
                    player.playerSkill.prepareLaze = true;
                    player.playerSkill.lastTimePrepareLaze = System.currentTimeMillis();
                    sendPlayerPrepareSkill(player, 1500);
                } else {
                    //bắn laze
                    player.playerSkill.prepareLaze = false;
                    if (plTarget != null) {
                        playerAttackPlayer(player, plTarget, false);
                    }
                    if (mobTarget != null) {
                        playerAttackMob(player, mobTarget, false, true);
                    }
                    affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                }
                PlayerService.gI().sendInfoHpMpMoney(player);
                break;
            case Skill.SOCOLA:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.SOCOLA);
                int timeSocola = SkillUtil.getTimeSocola();
                if (plTarget != null) {
                    EffectSkillService.gI().setSocola(plTarget, System.currentTimeMillis(), timeSocola);
                    Service.gI().Send_Caitrang(plTarget);
                    ItemTimeService.gI().sendItemTime(plTarget, 4133, timeSocola / 1000);
                }
                if (mobTarget != null) {
                    EffectSkillService.gI().sendMobToSocola(player, mobTarget, timeSocola);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                int timeChoangDCTT = SkillUtil.getTimeDCTT(player.playerSkill.skillSelect.point);
                if (plTarget != null) {
                    if (player.isBoss) {
                        Service.gI().chat(player, "Dịch chuyển tức thời");
                    }
                    Service.gI().setPos(player, plTarget.location.x, plTarget.location.y);
                    playerAttackPlayer(player, plTarget, miss);
                    EffectSkillService.gI().setBlindDCTT(plTarget, System.currentTimeMillis(), timeChoangDCTT);
                    EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.BLIND_EFFECT);
                    PlayerService.gI().sendInfoHpMpMoney(plTarget);
                    ItemTimeService.gI().sendItemTime(plTarget, 3779, timeChoangDCTT / 1000);
                }
                if (mobTarget != null) {
                    Service.gI().setPos(player, mobTarget.location.x, mobTarget.location.y);
                    playerAttackMob(player, mobTarget, false, false);
                    mobTarget.effectSkill.setStartBlindDCTT(System.currentTimeMillis(), timeChoangDCTT);
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.BLIND_EFFECT);
                }
                player.nPoint.isCrit100 = true;
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.THOI_MIEN:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.THOI_MIEN);
                int timeSleep = SkillUtil.getTimeThoiMien(player.playerSkill.skillSelect.point);
                if (plTarget != null) {
                    EffectSkillService.gI().setThoiMien(plTarget, System.currentTimeMillis(), timeSleep);
                    EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.SLEEP_EFFECT);
                    ItemTimeService.gI().sendItemTime(plTarget, 3782, timeSleep / 1000);
                }
                if (mobTarget != null) {
                    mobTarget.effectSkill.setThoiMien(System.currentTimeMillis(), timeSleep);
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.SLEEP_EFFECT);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.TROI:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.TROI);
                int timeHold = SkillUtil.getTimeTroi(player.playerSkill.skillSelect.point);
                if (plTarget instanceof Boss && ((Boss) plTarget).id == BossID.BABY) {
                    timeHold = 5000;
                }
                if (mobTarget instanceof GauTuongCuop) {
                    timeHold = 5000;
                }
                EffectSkillService.gI().setUseTroi(player, System.currentTimeMillis(), timeHold);
                if (plTarget != null && (!plTarget.playerSkill.prepareQCKK && !plTarget.playerSkill.prepareLaze && !plTarget.playerSkill.prepareTuSat)) {
                    player.effectSkill.plAnTroi = plTarget;
                    EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HOLD_EFFECT);
                    EffectSkillService.gI().setAnTroi(plTarget, player, System.currentTimeMillis(), timeHold);
                }
                if (mobTarget != null) {
                    player.effectSkill.mobAnTroi = mobTarget;
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HOLD_EFFECT);
                    mobTarget.effectSkill.setTroi(System.currentTimeMillis(), timeHold);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
        }
        if (!player.isBoss) {
            if (player.playerSkill != null && player.playerSkill.skillSelect != null && player.playerSkill.skillSelect.template != null) {
                int skillId = player.playerSkill.skillSelect.template.id;
                switch (skillId) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                    case Skill.DRAGON:
                    case Skill.DEMON:
                    case Skill.GALICK:
                    case Skill.LIEN_HOAN:
                    case Skill.KAIOKEN:
                    case Skill.QUA_CAU_KENH_KHI:
                    case Skill.MAKANKOSAPPO:
                    case Skill.DICH_CHUYEN_TUC_THOI:
                        player.effectSkin.lastTimeAttack = System.currentTimeMillis();
                        break;
                }
                AchievementService.gI().checkDoneTaskUseSkill(player);
                player.doesNotAttack = false;
                player.lastTimePlayerNotAttack = System.currentTimeMillis();
            } else {
            }
        }
    }

    public void useSkillAlone(Player player) {
        List<Mob> mobs;
        List<Player> players;
        switch (player.playerSkill.skillSelect.template.id) {
            case Skill.THAI_DUONG_HA_SAN:
                int timeStun = SkillUtil.getTimeStun(player.playerSkill.skillSelect.point);
                if (player.setClothes.thienXinHang == 5) {
                    timeStun *= 2;
                }
                mobs = new ArrayList<>();
                players = new ArrayList<>();
                if (!MapService.gI().isMapOffline(player.zone.map.mapId)) {
                    List<Player> playersMap;
                    if (player.isBoss) {
                        playersMap = player.zone.getNotBosses();
                    } else {
                        playersMap = player.zone.getHumanoids();
                    }
                    for (Player pl : playersMap) {
                        if (pl != null && !player.equals(pl) && pl.nPoint != null && !pl.nPoint.khangTDHS) {
                            if (Util.getDistance(player, pl) <= SkillUtil.getRangeStun(player.playerSkill.skillSelect.point)
                                    && canAttackPlayer(player, pl)) {
                                if (player.isPet && ((Pet) player).master.equals(pl)) {
                                    continue;
                                }
                                String[] text = {"Mắt của ta", "Chói mắt quá", "Đui mắt rồi", "Mù mắt rồi"};
                                Service.gI().chat(pl, text[Util.nextInt(text.length)]);
                                EffectSkillService.gI().startStun(pl, System.currentTimeMillis(), timeStun);
                                players.add(pl);
                            }
                        }
                    }
                }
                if (!player.isBoss) {
                    for (Mob mob : player.zone.mobs) {
                        if (Util.getDistance(player, mob) <= SkillUtil.getRangeStun(player.playerSkill.skillSelect.point)) {
                            mob.effectSkill.startStun(System.currentTimeMillis(), timeStun);
                            mobs.add(mob);
                        }
                    }
                }
                EffectSkillService.gI().sendEffectBlindThaiDuongHaSan(player, players, mobs, timeStun);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.DE_TRUNG:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.DE_TRUNG);
                if (player.mobMe != null) {
                    player.mobMe.mobMeDie();
                    player.mobMe.dispose();
                    player.mobMe = null;
                }
                player.mobMe = new MobMe(player);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.BIEN_KHI:
                EffectSkillService.gI().startUseSkillMonkey(player);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.KHIEN_NANG_LUONG:
                EffectSkillService.gI().setStartShield(player);
                EffectSkillService.gI().sendEffectPlayer(player, player, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.SHIELD_EFFECT);
                ItemTimeService.gI().sendItemTime(player, 3784, player.effectSkill.timeShield / 1000);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.HUYT_SAO:
                int tileHP = SkillUtil.getPercentHPHuytSao(player.playerSkill.skillSelect.point);
                if (player.zone != null) {
                    if (!MapService.gI().isMapOffline(player.zone.map.mapId)) {
                        if (!player.isBoss) {
                            List<Player> playersMap = player.zone.getHumanoids();
                            for (Player pl : playersMap) {
                                if (pl.effectSkill.useTroi) {
                                    EffectSkillService.gI().removeUseTroi(pl);
                                }
                                if (!pl.isBoss && pl.gender != ConstPlayer.NAMEC
                                        && player.cFlag == pl.cFlag) {
                                    EffectSkillService.gI().setStartHuytSao(pl, tileHP);
                                    EffectSkillService.gI().sendEffectPlayer(pl, pl, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HUYT_SAO_EFFECT);
                                    pl.nPoint.calPoint();
                                    pl.nPoint.setHp(pl.nPoint.hp + (pl.nPoint.hp * tileHP / 100));
                                    Service.gI().point(pl);
                                    Service.gI().Send_Info_NV(pl);
                                    ItemTimeService.gI().sendItemTime(pl, 3781, 30);
                                    PlayerService.gI().sendInfoHpMp(pl);
                                } else if (!pl.isBoss && pl.gender == ConstPlayer.NAMEC && player.cFlag == pl.cFlag) {
                                    long hpSub = pl.nPoint.hpMax * 10 / 100;
                                    pl.nPoint.setHP(pl.nPoint.hp - (hpSub < pl.nPoint.hp ? hpSub : 0));
                                    Service.gI().point(pl);
                                    Service.gI().Send_Info_NV(pl);
                                }
                            }
                        } else {
                            List<Player> playersMap = player.zone.getBosses();
                            for (Player pl : playersMap) {
                                if (pl.effectSkill.useTroi) {
                                    EffectSkillService.gI().removeUseTroi(pl);
                                }
                                EffectSkillService.gI().setStartHuytSao(pl, tileHP);
                                EffectSkillService.gI().sendEffectPlayer(pl, pl, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HUYT_SAO_EFFECT);
                                pl.nPoint.calPoint();
                                pl.nPoint.setHp(pl.nPoint.hp + (pl.nPoint.hp * tileHP / 100));
                                Service.gI().point(pl);
                                Service.gI().Send_Info_NV(pl);
                                ItemTimeService.gI().sendItemTime(pl, 3781, 30);
                                PlayerService.gI().sendInfoHpMp(pl);
                            }
                        }
                    } else {
                        EffectSkillService.gI().setStartHuytSao(player, tileHP);
                        EffectSkillService.gI().sendEffectPlayer(player, player, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.HUYT_SAO_EFFECT);
                        player.nPoint.calPoint();
                        player.nPoint.setHp(player.nPoint.hp + (player.nPoint.hp * tileHP / 100));
                        Service.gI().point(player);
                        Service.gI().Send_Info_NV(player);
                        ItemTimeService.gI().sendItemTime(player, 3781, 30);
                        PlayerService.gI().sendInfoHpMp(player);
                    }
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.TAI_TAO_NANG_LUONG:
                EffectSkillService.gI().startCharge(player);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                Service.gI().sendEffAllPlayer(player, 284, 1, -1, -1);
                break;
            case Skill.TU_SAT:
                if (!player.playerSkill.prepareTuSat) {
                    Skill tuSatSkill = player.playerSkill.skillSelect;
                    player.playerSkill.prepareTuSat = true;
                    player.playerSkill.lastTimePrepareTuSat = System.currentTimeMillis();
                    sendPlayerPrepareBom(player, 2000);
                    scheduleTuSat(player, tuSatSkill, player.playerSkill.lastTimePrepareTuSat);
                }
                break;
        }
    }

    private void scheduleTuSat(Player player, Skill tuSatSkill, long prepareTime) {
        Util.threadPool(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            explodeTuSat(player, tuSatSkill, prepareTime);
        });
    }

    private void explodeTuSat(Player player, Skill tuSatSkill, long prepareTime) {
        if (player == null || tuSatSkill == null || tuSatSkill.template == null
                || tuSatSkill.template.id != Skill.TU_SAT || player.playerSkill == null
                || player.nPoint == null || player.zone == null || player.zone.map == null) {
            return;
        }
        if (!player.playerSkill.prepareTuSat || player.playerSkill.lastTimePrepareTuSat != prepareTime || player.isDie()) {
            return;
        }

        player.playerSkill.skillSelect = tuSatSkill;
        player.playerSkill.prepareTuSat = false;
        int rangeBom = SkillUtil.getRangeBom(tuSatSkill.point);
        if (player.setClothes.cadicM == 2) {
            rangeBom += 200;
        }
        long dame = player.nPoint.hp;
        if (player.setClothes.cadicM == 4) {
            dame += player.nPoint.hpMax * 20 / 100;
        } else if (player.setClothes.cadicM == 5) {
            dame += player.nPoint.hpMax * 50 / 100;
        }
        if (!player.isBoss && player.zone.mobs != null) {
            for (Mob mob : new ArrayList<>(player.zone.mobs)) {
                if (mob != null && !mob.isDie() && Util.getDistance(player, mob) <= rangeBom) {
                    long hpBefore = mob.point.gethp();
                    mob.injured(player, dame, true);
                    announceHighDamage(player, Math.max(0, hpBefore - mob.point.gethp()));
                }
            }
        }
        List<Player> playersMap = player.isBoss
                ? new ArrayList<>(player.zone.getNotBosses())
                : new ArrayList<>(player.zone.getHumanoids());
        if (!MapService.gI().isMapOffline(player.zone.map.mapId)) {
            for (Player pl : playersMap) {
                if (pl != null && !player.equals(pl) && canAttackPlayer(player, pl) && Util.getDistance(player, pl) <= rangeBom) {
                    long damePlayer = dame;
                    if (pl.isBoss) {
                        damePlayer = applyDameBoss(player, pl, damePlayer);
                        damePlayer = player.effectSkill.isMonkey ? damePlayer / 3 : damePlayer / 2;
                    }
                    long hpBefore = pl.nPoint.hp;
                    pl.injured(player, limitDame(damePlayer), MapService.gI().isMapYardart(player.zone.map.mapId), false);
                    announceHighDamage(player, Math.max(0, hpBefore - pl.nPoint.hp));
                    PlayerService.gI().sendInfoHpMpMoney(pl);
                    Service.gI().Send_Info_NV(pl);
                }
            }
        }
        affterUseSkill(player, Skill.TU_SAT);
        if (!player.isBoss && !player.isPet && !player.isDie()) {
            player.setDie();
        }
        if (player.effectSkill != null && player.effectSkill.tiLeHPHuytSao != 0) {
            player.effectSkill.tiLeHPHuytSao = 0;
            EffectSkillService.gI().removeHuytSao(player);
        }
    }

    private void useSkillBuffToPlayer(Player player, Player plTarget) {
        Message msg = null;
        if (player.playerSkill.skillSelect.template.id == Skill.TRI_THUONG) {
            List<Player> players = new ArrayList<>();
            int percentTriThuong = SkillUtil.getPercentTriThuong(player.playerSkill.skillSelect.point);
            int point = player.playerSkill.skillSelect.point;
            if (canHsPlayer(player, plTarget) && plTarget.nPoint != null && player.zone != null) {
                players.add(plTarget);
                List<Player> playersMap = new ArrayList<>(player.zone.getNotBosses());
                for (Player pl : playersMap) {
                    if (pl != null && !pl.equals(player) && !pl.equals(plTarget) && point > 1) {
                        if (canHsPlayer(player, pl) && pl.nPoint != null && Util.getDistance(player, pl) <= 300) {
                            players.add(pl);
                        }
                    }
                }
                if (!players.contains(player)) {
                    player.nPoint.setHP(player.nPoint.getHP() + (player.nPoint.hpMax * percentTriThuong / 100));
                    Service.gI().Send_Info_NV(player);
                    PlayerService.gI().sendInfoHpMp(player);
                }
                for (Player pl : players) {
                    try {
                        msg = new Message(-60);
                        msg.writer().writeInt((int) player.id); //id pem
                        msg.writer().writeByte(player.playerSkill.skillSelect.skillId); //skill pem
                        msg.writer().writeByte(1); //số người pem
                        msg.writer().writeInt((int) pl.id); //id ăn pem
                        msg.writer().writeByte(0); //read continue
                        Service.gI().sendMessAllPlayerInMap(pl, msg);
                        boolean isDie = pl.isDie();
                        pl.nPoint.setHP(pl.nPoint.getHP() + (pl.nPoint.hpMax * percentTriThuong / 100));
                        pl.nPoint.setMP(pl.nPoint.getMP() + ((int) pl.nPoint.mpMax * percentTriThuong / 100));
                        if (isDie) {
                            AchievementService.gI().checkDoneTask(pl, ConstAchievement.CHAM_SOC_DAC_BIET);
                            Service.gI().chat(pl, "Cảm ơn " + player.name + " đã hồi sinh mình");
                            Service.gI().Send_Info_NV(player);
                            Service.gI().hsChar(pl, pl.nPoint.getHP(), pl.nPoint.getMP());
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            PlayerService.gI().sendInfoHpMp(player);
                        } else {
                            Service.gI().chat(pl, "Cảm ơn " + player.name + " đã cứu mình");
                            Service.gI().Send_Info_NV(player);
                            PlayerService.gI().sendInfoHpMp(pl);
                            PlayerService.gI().sendInfoHpMp(player);
                        }
                        Service.gI().Send_Info_NV(pl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (msg != null) {
                            msg.cleanup();
                        }
                    }
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
            }
        }
    }

    private boolean isMeleeAttackOutOfRange(Player player, Player target) {
        if (player == null || target == null || player.zone == null) {
            return false;
        }
        if (player.zone.map.mapId == 113) {
            return false;
        }
        if (MapService.gI().isMapVoDaiSieuCap(player.zone.map.mapId)) {
            // Map 145 has platform corners where the client can stop diagonally from
            // the target. Check each axis so a visually close punch is not marked miss.
            return Math.abs(player.location.x - target.location.x) > Skill.RANGE_ATTACK_CHIEU_DAM
                    || Math.abs(player.location.y - target.location.y) > Skill.RANGE_ATTACK_CHIEU_DAM;
        }
        return Util.getDistance(player, target) > Skill.RANGE_ATTACK_CHIEU_DAM;
    }

    private void phanSatThuong(Player plAtt, Player plTarget, long dame) {
        if (plAtt != null) {
            int percentPST = plTarget.nPoint.tlPST;
            if (percentPST != 0) {
                long damePST = dame * percentPST / 100L;
                Message msg = null;
                try {
                    msg = new Message(56);
                    msg.writer().writeInt((int) plAtt.id);
                    if (damePST >= plAtt.nPoint.hp) {
                        damePST = plAtt.nPoint.hp - 1;
                    }
                    if (plAtt.isBoss && !(plAtt instanceof Broly || plAtt instanceof SuperBroly)) {
                        if (damePST > plAtt.nPoint.hpMax / 100) {
                            int giamdame = 0;
                            if (plAtt.nPoint.hpMax / 200 > 1) {
                                giamdame = Util.nextInt((int) Math.min(plAtt.nPoint.hpMax / 200, Integer.MAX_VALUE));
                            }
                            damePST = plAtt.nPoint.hpMax / 100 - giamdame;
                        }
                    }
                    long hpBefore = plAtt.nPoint.hp;
                    plAtt.injured(plAtt, damePST, true, false);
                    int damePSTHit = NPoint.toClientStat(Math.max(0, hpBefore - plAtt.nPoint.hp));
                    msg.writer().writeInt(plAtt.nPoint.getClientHp());
                    msg.writer().writeInt(damePSTHit);
                    msg.writer().writeBoolean(false);
                    msg.writer().writeByte(36);
                    Service.gI().sendMessAllPlayerInMap(plAtt, msg);
                } catch (Exception e) {
                    Logger.logException(SkillService.class, e);
                } finally {
                    if (msg != null) {
                        msg.cleanup();
                    }
                }
            }
        }
    }

    private long applyDameBoss(Player plAtt, Player plInjure, long dame) {
        if (plAtt != null && plAtt.nPoint != null && plInjure != null && plInjure.isBoss && plAtt.nPoint.tlDameBoss > 0) {
            dame += dame * plAtt.nPoint.tlDameBoss / 100L;
        }
        return dame;
    }

    private long applyJackyChunChuongDame(Player plAtt, long dame) {
        if (!isJackyChunChuongReady(plAtt)) {
            return dame;
        }
        plAtt.lastTimeWearJackyChunCostume = System.currentTimeMillis();
        if (dame > Long.MAX_VALUE / JACKY_CHUN_CHUONG_MULTIPLIER) {
            return Long.MAX_VALUE;
        }
        return dame * JACKY_CHUN_CHUONG_MULTIPLIER;
    }

    private boolean isJackyChunChuongReady(Player plAtt) {
        if (plAtt == null || plAtt.inventory == null || plAtt.inventory.itemsBody == null
                || plAtt.playerSkill == null || plAtt.playerSkill.skillSelect == null
                || plAtt.playerSkill.skillSelect.template == null || !isJackyChunChuongSkill(plAtt)) {
            return false;
        }
        if (plAtt.inventory.itemsBody.size() <= 5) {
            plAtt.lastTimeWearJackyChunCostume = 0;
            return false;
        }
        Item costume = plAtt.inventory.itemsBody.get(5);
        if (costume == null || !costume.isNotNullItem() || costume.template.id != ConstItem.CAI_TRANG_JACKY_CHUN) {
            plAtt.lastTimeWearJackyChunCostume = 0;
            return false;
        }
        if (plAtt.lastTimeWearJackyChunCostume <= 0) {
            plAtt.lastTimeWearJackyChunCostume = System.currentTimeMillis();
            return false;
        }
        return Util.canDoWithTime(plAtt.lastTimeWearJackyChunCostume, JACKY_CHUN_CHUONG_DELAY);
    }

    private boolean isJackyChunChuongSkill(Player plAtt) {
        int skillId = plAtt.playerSkill.skillSelect.template.id;
        return skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC
                || skillId == Skill.SUPER_KAME || skillId == Skill.LIEN_HOAN_CHUONG;
    }

    private long limitDame(long dame) {
        return Math.min(dame, NPoint.MAX_PLAYER_DAME);
    }

    private void announceHighDamage(Player plAtt, long damage) {
        if (damage <= HIGH_DAMAGE_ANNOUNCE_THRESHOLD) {
            return;
        }
        Player announcer = getHighDamageAnnouncer(plAtt);
        if (announcer == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long lastAnnounce = lastHighDamageAnnouncements.get(announcer.id);
        if (lastAnnounce != null && now - lastAnnounce < HIGH_DAMAGE_ANNOUNCE_COOLDOWN) {
            return;
        }
        lastHighDamageAnnouncements.put(announcer.id, now);
        Service.gI().sendThongBaoAllPlayer("Người chơi " + getHighDamageAttackerName(plAtt, announcer)
                + " vừa tung ra chiêu " + getSelectedSkillName(plAtt)
                + " với sát thương " + Util.formatNumber(damage));
    }

    private Player getHighDamageAnnouncer(Player plAtt) {
        if (plAtt == null || plAtt.isBoss || plAtt.isBot || plAtt.isNewPet || plAtt.isNewPet1) {
            return null;
        }
        if (plAtt.isPl()) {
            return plAtt;
        }
        if (plAtt.isPet && plAtt instanceof Pet) {
            Player master = ((Pet) plAtt).master;
            if (master != null && master.isPl()) {
                return master;
            }
        }
        return null;
    }

    private String getHighDamageAttackerName(Player plAtt, Player announcer) {
        if (plAtt != null && plAtt.isPet && plAtt.name != null && !plAtt.name.equals(announcer.name)) {
            return announcer.name + " (" + plAtt.name + ")";
        }
        return announcer.name;
    }

    private String getSelectedSkillName(Player plAtt) {
        if (plAtt == null || plAtt.playerSkill == null || plAtt.playerSkill.skillSelect == null) {
            return "khong ro";
        }
        Skill skill = plAtt.playerSkill.skillSelect;
        if (skill.template != null && skill.template.name != null && !skill.template.name.isEmpty()) {
            return skill.template.name;
        }
        return "Skill " + skill.skillId;
    }

    private void hutHPMP(Player player, long dame, Player pl, Mob mob) {
        int tiLeHutHp = player.nPoint.getTileHutHp(mob != null);
        int tiLeHutMp = player.nPoint.getTiLeHutMp();
        long hpHoi = dame * tiLeHutHp / 100;
        long mpHoi = dame * tiLeHutMp / 100;
        if (hpHoi > 0 || mpHoi > 0) {
            int x = -1;
            int y = -1;
            if (pl != null) {
                x = pl.location.x;
                y = pl.location.y;
            } else if (mob != null) {
                x = mob.location.x;
                y = mob.location.y;
            }
            EffectMapService.gI().sendEffectMapToAllInMap(player, 37, 3, 1, x, y, -1);
            PlayerService.gI().hoiPhuc(player, hpHoi, mpHoi);
        }
    }

    private void playerAttackPlayer(Player plAtt, Player plInjure, boolean miss) {
        if (plInjure.effectSkill.anTroi) {
            plAtt.nPoint.isCrit100 = true;
        }
        long dameAttack = applyDameBoss(plAtt, plInjure, plAtt.nPoint.getDameAttack(false));
        if (plAtt.isPl() && plAtt.effectSkin != null && plAtt.effectSkin.isXDame) {
            plAtt.effectSkin.isXDame = false;
            if (plInjure.isBoss) {
                dameAttack /= 3;
            }
        }
        if (!miss) {
            dameAttack = applyJackyChunChuongDame(plAtt, dameAttack);
        }
        dameAttack = limitDame(dameAttack);
        long hpBefore = plInjure.nPoint.hp;
        plInjure.injured(plAtt, miss ? 0 : dameAttack, false, false);
        long dameHit = Math.max(0, hpBefore - plInjure.nPoint.hp);
        int clientDameHit = NPoint.toClientStat(dameHit);
        if (plAtt.playerSkill == null) {
            return;
        }
        announceHighDamage(plAtt, dameHit);
        Skill skillSelect = plAtt.playerSkill.skillSelect;
        long damePST = plInjure.effectSkill != null && plInjure.effectSkill.isShielding && plInjure.idMark != null
                ? plInjure.idMark.getDamePST() : dameHit;
        phanSatThuong(plAtt, plInjure, miss ? 0 : damePST);
        hutHPMP(plAtt, dameHit, plInjure, null);
        if (plInjure instanceof Yardart) {
            if (plInjure.nPoint.hp < dameHit) {
                dameHit = Math.max(0, plInjure.nPoint.hp - 1);
                clientDameHit = NPoint.toClientStat(dameHit);
                if (clientDameHit == 0) {
                    return;
                }
            } else if (plInjure.nPoint.hp <= plInjure.nPoint.hpMax / 10) {
                return;
            }
        }
        Message msg = null;
        try {
            msg = new Message(-60);
            msg.writer().writeInt((int) plAtt.id); //id pem
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.skillId); //skill pem
            msg.writer().writeByte(1); //số người pem
            msg.writer().writeInt((int) plInjure.id); //id ăn pem
            msg.writer().writeByte(1); //read continue
            msg.writer().writeByte(0); //type skill
            msg.writer().writeInt(clientDameHit); //dame ăn
            msg.writer().writeBoolean(plInjure.isDie()); //is die
            msg.writer().writeBoolean(plAtt.nPoint.isCrit); //crit
            Service.gI().sendMessAllPlayerInMap(plAtt, msg);
            Service.gI().reload_HP_NV(plInjure);
            if (plAtt.isPl() && plInjure.isPl() && plAtt.typePk == ConstPlayer.PK_PVP_2 && plInjure.typePk == ConstPlayer.PK_PVP_2) {
                long tnsm = plAtt.nPoint.calSucManhTiemNang(dameHit / 10) / (Math.abs(Service.gI().getCurrLevel(plAtt) - Service.gI().getCurrLevel(plInjure)) + 1);
                Service.gI().addSMTN(plInjure, (byte) 2, tnsm, false);
            }
            if (plInjure.isDie() && !plAtt.isBoss && !plInjure.isBoss && MapService.gI().isMapMaBu(plInjure.zone.map.mapId)) {
                plAtt.fightMabu.changePoint((byte) 5);
            }
        } catch (Exception e) {
            Logger.logException(SkillService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void playerAttackMob(Player plAtt, Mob mob, boolean miss, boolean dieWhenHpFull) {
        if (mob == null || mob.isDie() || plAtt == null || plAtt.nPoint == null || plAtt.playerSkill == null) {
            return;
        }

        long dameHit = plAtt.nPoint.getDameAttack(true);

        if (plAtt.effectSkin != null && plAtt.effectSkin.isXDame) {
            plAtt.effectSkin.isXDame = false;
        }

        if (plAtt.charms != null && plAtt.charms.tdBatTu > System.currentTimeMillis() && plAtt.nPoint.hp <= 1) {
            if (plAtt.nPoint.hp < 1) {
                plAtt.nPoint.hp = 1;
            }
            if (!plAtt.isPet) {
                dameHit = 0;
                Service.gI().sendThongBao(plAtt, "Bạn đang được bùa bất tử bảo vệ không thể tấn công!");
            }
        }

        if (plAtt.charms != null && plAtt.charms.tdManhMe > System.currentTimeMillis()) {
            dameHit += (dameHit * 150 / 100);
        }

        if (plAtt.isPet) {
            Pet pet = (Pet) plAtt;
            if (pet.master != null && pet.master.charms != null && pet.master.charms.tdDeTu > System.currentTimeMillis()) {
                dameHit *= 2;
            }
        }

        if (miss) {
            dameHit = 0;
        }

        dameHit = Math.min(dameHit, NPoint.MAX_PLAYER_DAME);

        hutHPMP(plAtt, dameHit, null, mob);
        sendPlayerAttackMob(plAtt, mob);
        long hpBefore = mob.point.gethp();
        mob.injured(plAtt, dameHit, dieWhenHpFull);
        announceHighDamage(plAtt, Math.max(0, hpBefore - mob.point.gethp()));
    }

    public void sendPlayerPrepareSkill(Player player, int affterMiliseconds) {
        Message msg = null;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(4);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(player.playerSkill.skillSelect.skillId);
            msg.writer().writeShort(affterMiliseconds);
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendPlayerPrepareBom(Player player, int affterMiliseconds) {
        Message msg = null;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(7);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(player.playerSkill.skillSelect.skillId);
            msg.writer().writeShort(affterMiliseconds);
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public boolean canUseSkillWithMana(Player player) {
        if (player.playerSkill.skillSelect != null) {
            if (player.playerSkill.skillSelect.template.id == Skill.KAIOKEN) {
                long hpUse = player.nPoint.hpMax / 100 * 5; // Đồng bộ: check 5% HP
                int kiUse = 10000; // Check 10k KI
                if (player.isBoss && player instanceof Rival) {
                    hpUse = 0;
                    kiUse = 0;
                }
                if (player.nPoint.hp <= hpUse || player.nPoint.mp < kiUse) {
                    return false;
                }
            }
            switch (player.playerSkill.skillSelect.template.manaUseType) {
                case 0 -> {
                    return player.nPoint.mp >= player.playerSkill.skillSelect.manaUse;
                }
                case 1 -> {
                    int mpUse = (player.nPoint.mpMax * player.playerSkill.skillSelect.manaUse / 100);
                    return player.nPoint.mp >= mpUse;
                }
                case 2 -> {
                    return player.nPoint.mp > 0;
                }
                default -> {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean canUseSkillWithCooldown(Player player) {
        return Util.canDoWithTime(player.playerSkill.skillSelect.lastTimeUseThisSkill,
                player.playerSkill.skillSelect.coolDown - 50);
    }

    public void affterUseSkill(Player player, int skillId) {
        Intrinsic intrinsic = player.playerIntrinsic.intrinsic;
        switch (skillId) {
            case Skill.DICH_CHUYEN_TUC_THOI -> {
                if (intrinsic.id == 6) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
            }
            case Skill.THOI_MIEN -> {
                if (intrinsic.id == 7) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
            }
            case Skill.SOCOLA -> {
                if (intrinsic.id == 14) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
            }
            case Skill.TROI -> {
                if (intrinsic.id == 22) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
            }
        }
        setMpAffterUseSkill(player);
        setLastTimeUseSkill(player, skillId);
    }

    private void setMpAffterUseSkill(Player player) {
        if (player.playerSkill.skillSelect != null) {
            switch (player.playerSkill.skillSelect.template.manaUseType) {
                case 0 -> {
                    if (player.nPoint.mp >= player.playerSkill.skillSelect.manaUse) {
                        player.nPoint.setMp(player.nPoint.mp - player.playerSkill.skillSelect.manaUse);
                    }
                }
                case 1 -> {
                    int mpUse = (int) (player.nPoint.mpMax * player.playerSkill.skillSelect.manaUse / 100);
                    if (player.nPoint.mp >= mpUse) {
                        player.nPoint.setMp(player.nPoint.mp - mpUse);
                    }
                }
                case 2 ->
                    player.nPoint.setMp(0);
            }
            PlayerService.gI().sendInfoHpMpMoney(player);
        }
    }

    private void setLastTimeUseSkill(Player player, int skillId) {
        Intrinsic intrinsic = player.playerIntrinsic.intrinsic;
        int subTimeParam = 0;
        switch (skillId) {
            case Skill.TRI_THUONG -> {
                if (intrinsic.id == 10) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.THAI_DUONG_HA_SAN -> {
                if (intrinsic.id == 3) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.QUA_CAU_KENH_KHI -> {
                if (intrinsic.id == 4) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.KHIEN_NANG_LUONG -> {
                if (intrinsic.id == 5 || intrinsic.id == 15 || intrinsic.id == 20) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.MAKANKOSAPPO -> {
                if (intrinsic.id == 11) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.DE_TRUNG -> {
                if (intrinsic.id == 12) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.TU_SAT -> {
                if (intrinsic.id == 19) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.HUYT_SAO -> {
                if (intrinsic.id == 21) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.MASENKO -> {
                if (intrinsic.id == 9) {
                    subTimeParam = intrinsic.param1;
                }
                if (player.setClothes.nail == 4) {
                    subTimeParam = subTimeParam + 20; // Nếu nail = 4, cộng thêm 20
                }

                if (player.setClothes.nail == 5) {
                    subTimeParam = subTimeParam + 50; // Nếu nail = 5, cộng thêm 50
                }
            }
        }
        player.playerSkill.skillSelect.lastTimeUseThisSkill = System.currentTimeMillis() - 1;
        int coolDown = player.playerSkill.skillSelect.coolDown;
        long lastTimeUseSkill = System.currentTimeMillis() - ((long) coolDown * subTimeParam / 100);
        if (subTimeParam != 0) {
            EffectSkillService.gI().setIntrinsic(player, skillId, coolDown, lastTimeUseSkill);
        }
    }

    private boolean canHsPlayer(Player player, Player plTarget) {
        if (plTarget == null) {
            return false;
        }
        if (plTarget.isBoss) {
            return false;
        }
        if (plTarget.typePk == ConstPlayer.PK_ALL) {
            return false;
        }
        if (plTarget.typePk == ConstPlayer.PK_PVP) {
            return false;
        }
        if (player.cFlag != 0) {
            if (plTarget.cFlag != 0 && plTarget.cFlag != player.cFlag) {
                return false;
            }
        } else {
            return plTarget.cFlag == 0;
        }
        return true;
    }

    public boolean canAttackPlayer(Player p1, Player p2) {
        if (p1.isDie() || p2.isDie()) {
            return false;
        }

        return canAttackPlayer2(p1, p2);
    }

    public boolean canAttackPlayer2(Player p1, Player p2) {

        if (p1.isNewPet || p2.isNewPet || (p1 instanceof NonInteractiveNPC) || (p2 instanceof NonInteractiveNPC)) {
            return false;
        }

        if (p1.typePk == ConstPlayer.PK_ALL || p2.typePk == ConstPlayer.PK_ALL) {
            return true;
        }
        if (p1.isPl() && p2.isPl() && (p1.idMark != null && p1.idMark.getKillCharId() == p2.id
                || p2.idMark != null && p2.idMark.getKillCharId() == p1.id)) {
            return true;
        }
        if ((p1.cFlag != 0 && p2.cFlag != 0)
                && (p1.cFlag == 8 || p2.cFlag == 8 || p1.cFlag != p2.cFlag)) {
            return true;
        }
        if (p1.pvp == null || p2.pvp == null) {
            return false;
        }
        return p1.pvp.isInPVP(p2) || p2.pvp.isInPVP(p1);
    }

    private void sendPlayerAttackMob(Player plAtt, Mob mob) {
        Message msg = null;
        try {
            msg = new Message(54);
            msg.writer().writeInt((int) plAtt.id);
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.skillId);
            msg.writer().writeByte(mob.id);
            Service.gI().sendMessAllPlayerInMap(plAtt, msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void selectSkill(Player player, int skillId) {
        for (Skill skill : player.playerSkill.skills) {
            if (skill.skillId != -1 && skill.template.id == skillId) {
                player.playerSkill.skillSelect = skill;
                break;
            }
        }
    }
}
