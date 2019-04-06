package cn.mrcode.cachepdp.eshop.inventory.model;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/4/6 13:50
 */
public class ProductInventory {
    /**
     * 商品库存数量
     */
    private Integer productId;
    private Long inventoryCnt;

    public ProductInventory() {
    }

    public ProductInventory(Integer productId, Long inventoryCnt) {
        this.productId = productId;
        this.inventoryCnt = inventoryCnt;
    }

    public Long getInventoryCnt() {
        return inventoryCnt;
    }

    public void setInventoryCnt(Long inventoryCnt) {
        this.inventoryCnt = inventoryCnt;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}
