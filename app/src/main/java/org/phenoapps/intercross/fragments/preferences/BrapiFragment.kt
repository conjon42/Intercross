package org.phenoapps.intercross.fragments.preferences

/**
 * Extends PhenoLib BrapiFragment with toolbar navigation.
 */
//TODO
//class BrapiFragment : org.phenoapps.fragments.preferences.BrapiFragment(),
//    Preference.OnPreferenceChangeListener {
//
//    private var mBottomNavBar: BottomNavigationView? = null
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        mBottomNavBar = view.findViewById(R.id.preferences_bottom_nav_bar)
//
//        mBottomNavBar?.selectedItemId = R.id.action_nav_settings
//
//        setupBottomNavBar()
//
//        setHasOptionsMenu(false)
//    }
//
//    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}
//
//    private fun setupBottomNavBar() {
//
//        mBottomNavBar?.setOnNavigationItemSelectedListener { item ->
//
//            when (item.itemId) {
//
//                R.id.action_nav_home -> {
//
//                    findNavController().navigate(R.id.global_action_to_events)
//                }
//                R.id.action_nav_parents -> {
//
//                    findNavController().navigate(R.id.global_action_to_parents)
//                }
//                R.id.action_nav_export -> {
//
//                    (activity as MainActivity).showExportDialog {
//
//                        mBottomNavBar?.selectedItemId = R.id.action_nav_settings
//
//                    }
//                }
//                R.id.action_nav_cross_count -> {
//
//                    findNavController().navigate(R.id.global_action_to_cross_count)
//                }
//            }
//
//            true
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        (activity as MainActivity).supportActionBar?.show()
//
//        mBottomNavBar?.selectedItemId = R.id.action_nav_settings
//
//    }
//}