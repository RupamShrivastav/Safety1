// Generated by view binder compiler. Do not edit!
package com.example.safety.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.safety.R;
import com.google.android.material.card.MaterialCardView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentServiceBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView cv1Disc;

  @NonNull
  public final TextView cv1Head;

  @NonNull
  public final TextView cv2Disc;

  @NonNull
  public final TextView cv2Head;

  @NonNull
  public final MaterialCardView cvGuard;

  @NonNull
  public final MaterialCardView cvSOS;

  @NonNull
  public final ImageView guardImg;

  @NonNull
  public final ImageView sosImg;

  @NonNull
  public final TextView textSrc;

  private FragmentServiceBinding(@NonNull ConstraintLayout rootView, @NonNull TextView cv1Disc,
      @NonNull TextView cv1Head, @NonNull TextView cv2Disc, @NonNull TextView cv2Head,
      @NonNull MaterialCardView cvGuard, @NonNull MaterialCardView cvSOS,
      @NonNull ImageView guardImg, @NonNull ImageView sosImg, @NonNull TextView textSrc) {
    this.rootView = rootView;
    this.cv1Disc = cv1Disc;
    this.cv1Head = cv1Head;
    this.cv2Disc = cv2Disc;
    this.cv2Head = cv2Head;
    this.cvGuard = cvGuard;
    this.cvSOS = cvSOS;
    this.guardImg = guardImg;
    this.sosImg = sosImg;
    this.textSrc = textSrc;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentServiceBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentServiceBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_service, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentServiceBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.cv1Disc;
      TextView cv1Disc = ViewBindings.findChildViewById(rootView, id);
      if (cv1Disc == null) {
        break missingId;
      }

      id = R.id.cv1Head;
      TextView cv1Head = ViewBindings.findChildViewById(rootView, id);
      if (cv1Head == null) {
        break missingId;
      }

      id = R.id.cv2Disc;
      TextView cv2Disc = ViewBindings.findChildViewById(rootView, id);
      if (cv2Disc == null) {
        break missingId;
      }

      id = R.id.cv2Head;
      TextView cv2Head = ViewBindings.findChildViewById(rootView, id);
      if (cv2Head == null) {
        break missingId;
      }

      id = R.id.cvGuard;
      MaterialCardView cvGuard = ViewBindings.findChildViewById(rootView, id);
      if (cvGuard == null) {
        break missingId;
      }

      id = R.id.cvSOS;
      MaterialCardView cvSOS = ViewBindings.findChildViewById(rootView, id);
      if (cvSOS == null) {
        break missingId;
      }

      id = R.id.guardImg;
      ImageView guardImg = ViewBindings.findChildViewById(rootView, id);
      if (guardImg == null) {
        break missingId;
      }

      id = R.id.sosImg;
      ImageView sosImg = ViewBindings.findChildViewById(rootView, id);
      if (sosImg == null) {
        break missingId;
      }

      id = R.id.textSrc;
      TextView textSrc = ViewBindings.findChildViewById(rootView, id);
      if (textSrc == null) {
        break missingId;
      }

      return new FragmentServiceBinding((ConstraintLayout) rootView, cv1Disc, cv1Head, cv2Disc,
          cv2Head, cvGuard, cvSOS, guardImg, sosImg, textSrc);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
