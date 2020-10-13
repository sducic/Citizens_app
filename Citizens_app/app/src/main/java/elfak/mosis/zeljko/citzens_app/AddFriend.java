package elfak.mosis.zeljko.citzens_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class AddFriend extends AppCompatActivity {

    Button btnAllUsers;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private FriendsFragment friendsFragment;
    private RequestsFragment requestsFragment;
    private AllUsersFragment allUsersFragment;

    private RecyclerView mUsersList;
    private EditText mSearch;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        //toolbar = (Toolbar)findViewById(R.id.toolbar_fr);
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        tabLayout = (TabLayout)findViewById(R.id.tab_layout);

        friendsFragment = new FriendsFragment();
        requestsFragment = new RequestsFragment();
        allUsersFragment = new AllUsersFragment();

        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);

        viewPagerAdapter.addFragment(friendsFragment, "Friends");
        viewPagerAdapter.addFragment(requestsFragment, "Requests");
        viewPagerAdapter.addFragment(allUsersFragment, "Explore");
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.getTabAt(0).setIcon(R.drawable.baseline_people_white_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_action_group);
        tabLayout.getTabAt(2).setIcon(R.drawable.baseline_explore_white_24);



    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<Fragment>();
        private List<String> fragmentTitles = new ArrayList<String>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
}
