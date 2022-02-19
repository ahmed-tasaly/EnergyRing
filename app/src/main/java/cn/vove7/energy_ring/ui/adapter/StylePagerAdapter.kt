package cn.vove7.energy_ring.ui.adapter

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.vove7.energy_ring.ui.fragment.DoubleRingStyleFragment
import cn.vove7.energy_ring.ui.fragment.PillStyleFragment
import cn.vove7.energy_ring.ui.fragment.RingStyleFragment

/**
 * # StylePagerAdapter
 *
 * @author Vove
 * 2020/5/11
 */
class StylePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fs =
        listOf(
            RingStyleFragment(),
            DoubleRingStyleFragment(),
            PillStyleFragment()
        )

    override fun getItemCount(): Int = fs.size

    override fun createFragment(position: Int) = fs[position]

}