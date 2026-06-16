package com.familyfood.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.common.BizException;
import com.familyfood.entity.Category;
import com.familyfood.entity.Dish;
import com.familyfood.entity.Family;
import com.familyfood.entity.FamilyMember;
import com.familyfood.mapper.CategoryMapper;
import com.familyfood.mapper.DishMapper;
import com.familyfood.mapper.FamilyMapper;
import com.familyfood.mapper.FamilyMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    @Transactional
    public Family createFamily(Long userId, String name) {
        // 检查是否已在家庭中
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getUserId, userId));
        if (count > 0) throw new BizException("您已在一个家庭中，请先退出");

        Family family = new Family();
        family.setName(name);
        family.setCreatorId(userId);
        family.setInviteCode(generateCode());
        familyMapper.insert(family);

        // 创建者自动加入
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        memberMapper.insert(member);

        // 初始化默认菜品
        initDefaultDishes(family.getId());

        return family;
    }

    @Transactional
    public Family joinFamily(Long userId, String inviteCode) {
        Family family = familyMapper.selectOne(
                new LambdaQueryWrapper<Family>().eq(Family::getInviteCode, inviteCode));
        if (family == null) throw new BizException("邀请码无效");

        Long count = memberMapper.selectCount(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getFamilyId, family.getId()));
        if (count > 0) throw new BizException("您已在该家庭中");

        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        memberMapper.insert(member);

        return family;
    }

    public Family getFamilyInfo(Long familyId) {
        return familyMapper.selectById(familyId);
    }

    public List<FamilyMember> getMembers(Long familyId) {
        return memberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, familyId));
    }

    @Transactional
    public void leaveFamily(Long userId, Long familyId) {
        Family family = familyMapper.selectById(familyId);
        if (family != null && family.getCreatorId().equals(userId)) {
            // 创建者离开 = 解散家庭
            memberMapper.delete(new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, familyId));
            familyMapper.deleteById(familyId);
        } else {
            memberMapper.delete(new LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getUserId, userId)
                    .eq(FamilyMember::getFamilyId, familyId));
        }
    }

    private Category addCategory(Long familyId, String name, int sort) {
        Category c = new Category();
        c.setFamilyId(familyId);
        c.setName(name);
        c.setSortOrder(sort);
        categoryMapper.insert(c);
        return c;
    }

    private String img(String name) {
        try {
            return "/uploads/" + java.net.URLEncoder.encode(name + ".png", "UTF-8").replace("+", "%20");
        } catch (Exception e) { return null; }
    }

    private void addDish(Long familyId, Long cateId, String name, double price) {
        Dish d = new Dish();
        d.setFamilyId(familyId);
        d.setCategoryId(cateId);
        d.setName(name);
        d.setPrice(BigDecimal.valueOf(price));
        d.setImageUrl(img(name));
        d.setStatus(1);
        dishMapper.insert(d);
    }

    private void initDefaultDishes(Long familyId) {
        Category c1 = addCategory(familyId, "家常菜", 1);
        Category c2 = addCategory(familyId, "主食", 2);
        Category c3 = addCategory(familyId, "汤品", 3);
        Category c4 = addCategory(familyId, "凉菜", 4);
        Category c5 = addCategory(familyId, "小吃", 5);
        Category c6 = addCategory(familyId, "饮品", 6);
        Category c7 = addCategory(familyId, "甜品", 7);

        addDish(familyId, c1.getId(), "红烧肉", 28.0);
        addDish(familyId, c1.getId(), "番茄炒蛋", 12.0);
        addDish(familyId, c1.getId(), "糖醋排骨", 35.0);
        addDish(familyId, c1.getId(), "宫保鸡丁", 22.0);

        addDish(familyId, c2.getId(), "米饭", 2.0);
        addDish(familyId, c2.getId(), "馒头", 1.0);
        addDish(familyId, c2.getId(), "面条", 8.0);
        addDish(familyId, c2.getId(), "蛋炒饭", 10.0);

        addDish(familyId, c3.getId(), "紫菜蛋花汤", 8.0);
        addDish(familyId, c3.getId(), "番茄蛋汤", 8.0);
        addDish(familyId, c3.getId(), "排骨汤", 18.0);
        addDish(familyId, c3.getId(), "酸辣汤", 10.0);

        addDish(familyId, c4.getId(), "凉拌黄瓜", 8.0);
        addDish(familyId, c4.getId(), "皮蛋豆腐", 12.0);
        addDish(familyId, c4.getId(), "蒜泥白肉", 22.0);
        addDish(familyId, c4.getId(), "口水鸡", 25.0);

        addDish(familyId, c5.getId(), "春卷", 12.0);
        addDish(familyId, c5.getId(), "煎饺", 15.0);
        addDish(familyId, c5.getId(), "葱油饼", 8.0);
        addDish(familyId, c5.getId(), "炸鸡翅", 18.0);

        addDish(familyId, c6.getId(), "可乐", 5.0);
        addDish(familyId, c6.getId(), "雪碧", 5.0);
        addDish(familyId, c6.getId(), "橙汁", 8.0);
        addDish(familyId, c6.getId(), "酸梅汤", 6.0);

        addDish(familyId, c7.getId(), "银耳羹", 10.0);
        addDish(familyId, c7.getId(), "红豆汤", 8.0);
        addDish(familyId, c7.getId(), "绿豆糕", 12.0);
        addDish(familyId, c7.getId(), "水果拼盘", 15.0);
    }

    public void updateSettings(Long familyId, String orderDeadline) {
        Family family = familyMapper.selectById(familyId);
        if (family != null && orderDeadline != null) {
            family.setOrderDeadline(orderDeadline);
            familyMapper.updateById(family);
        }
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
