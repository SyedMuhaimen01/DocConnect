package com.ahmadmustafa.docconnect
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
class viewPagerViewCenterAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 3
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                TreatmentsFragment()
            }
            1 -> {
                SpecialistsFragment()
            }
            2-> {
                ReviewsFragment()
            }
            else -> {
                TreatmentsFragment()
            }
        }
    }
}