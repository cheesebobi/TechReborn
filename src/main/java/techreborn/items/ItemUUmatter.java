package techreborn.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import techreborn.client.TechRebornCreativeTab;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class ItemUUmatter extends Item{
	
	public ItemUUmatter()
	{
		setCreativeTab(TechRebornCreativeTab.instance);
		setUnlocalizedName("techreborn.uumattaer");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon("techreborn:"+ "uumatter");
	}

}
