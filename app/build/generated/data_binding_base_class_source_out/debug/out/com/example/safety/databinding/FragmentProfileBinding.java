// Generated by view binder compiler. Do not edit!
package com.example.safety.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.safety.R;
import com.google.android.material.card.MaterialCardView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentProfileBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final TextView email;

  @NonNull
  public final LinearLayout guardNum;

  @NonNull
  public final MaterialCardView inviteContacts;

  @NonNull
  public final ImageView profileImg;

  @NonNull
  public final TextView profileName;

  @NonNull
  public final TextView signout;

  @NonNull
  public final TextView tVProfile;

  @NonNull
  public final TextView trustedContactNameProfile;

  @NonNull
  public final TextView updateName;

  @NonNull
  public final TextView updateNumber;

  @NonNull
  public final TextView updatePassword;

  @NonNull
  public final TextView updatePin;

  private FragmentProfileBinding(@NonNull ScrollView rootView, @NonNull TextView email,
      @NonNull LinearLayout guardNum, @NonNull MaterialCardView inviteContacts,
      @NonNull ImageView profileImg, @NonNull TextView profileName, @NonNull TextView signout,
      @NonNull TextView tVProfile, @NonNull TextView trustedContactNameProfile,
      @NonNull TextView updateName, @NonNull TextView updateNumber,
      @NonNull TextView updatePassword, @NonNull TextView updatePin) {
    this.rootView = rootView;
    this.email = email;
    this.guardNum = guardNum;
    this.inviteContacts = inviteContacts;
    this.profileImg = profileImg;
    this.profileName = profileName;
    this.signout = signout;
    this.tVProfile = tVProfile;
    this.trustedContactNameProfile = trustedContactNameProfile;
    this.updateName = updateName;
    this.updateNumber = updateNumber;
    this.updatePassword = updatePassword;
    this.updatePin = updatePin;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentProfileBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentProfileBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_profile, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentProfileBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.email;
      TextView email = ViewBindings.findChildViewById(rootView, id);
      if (email == null) {
        break missingId;
      }

      id = R.id.guardNum;
      LinearLayout guardNum = ViewBindings.findChildViewById(rootView, id);
      if (guardNum == null) {
        break missingId;
      }

      id = R.id.inviteContacts;
      MaterialCardView inviteContacts = ViewBindings.findChildViewById(rootView, id);
      if (inviteContacts == null) {
        break missingId;
      }

      id = R.id.profileImg;
      ImageView profileImg = ViewBindings.findChildViewById(rootView, id);
      if (profileImg == null) {
        break missingId;
      }

      id = R.id.profileName;
      TextView profileName = ViewBindings.findChildViewById(rootView, id);
      if (profileName == null) {
        break missingId;
      }

      id = R.id.signout;
      TextView signout = ViewBindings.findChildViewById(rootView, id);
      if (signout == null) {
        break missingId;
      }

      id = R.id.tVProfile;
      TextView tVProfile = ViewBindings.findChildViewById(rootView, id);
      if (tVProfile == null) {
        break missingId;
      }

      id = R.id.trustedContactNameProfile;
      TextView trustedContactNameProfile = ViewBindings.findChildViewById(rootView, id);
      if (trustedContactNameProfile == null) {
        break missingId;
      }

      id = R.id.updateName;
      TextView updateName = ViewBindings.findChildViewById(rootView, id);
      if (updateName == null) {
        break missingId;
      }

      id = R.id.updateNumber;
      TextView updateNumber = ViewBindings.findChildViewById(rootView, id);
      if (updateNumber == null) {
        break missingId;
      }

      id = R.id.updatePassword;
      TextView updatePassword = ViewBindings.findChildViewById(rootView, id);
      if (updatePassword == null) {
        break missingId;
      }

      id = R.id.updatePin;
      TextView updatePin = ViewBindings.findChildViewById(rootView, id);
      if (updatePin == null) {
        break missingId;
      }

      return new FragmentProfileBinding((ScrollView) rootView, email, guardNum, inviteContacts,
          profileImg, profileName, signout, tVProfile, trustedContactNameProfile, updateName,
          updateNumber, updatePassword, updatePin);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
