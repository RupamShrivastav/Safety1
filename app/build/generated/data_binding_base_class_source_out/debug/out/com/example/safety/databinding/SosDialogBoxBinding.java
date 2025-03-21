// Generated by view binder compiler. Do not edit!
package com.example.safety.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

public final class SosDialogBoxBinding implements ViewBinding {
  @NonNull
  private final MaterialCardView rootView;

  @NonNull
  public final LinearLayout number100;

  @NonNull
  public final LinearLayout number101;

  @NonNull
  public final LinearLayout number102;

  @NonNull
  public final LinearLayout number112;

  @NonNull
  public final LinearLayout number139;

  @NonNull
  public final TextView trustedContName;

  @NonNull
  public final LinearLayout trustedContact;

  private SosDialogBoxBinding(@NonNull MaterialCardView rootView, @NonNull LinearLayout number100,
      @NonNull LinearLayout number101, @NonNull LinearLayout number102,
      @NonNull LinearLayout number112, @NonNull LinearLayout number139,
      @NonNull TextView trustedContName, @NonNull LinearLayout trustedContact) {
    this.rootView = rootView;
    this.number100 = number100;
    this.number101 = number101;
    this.number102 = number102;
    this.number112 = number112;
    this.number139 = number139;
    this.trustedContName = trustedContName;
    this.trustedContact = trustedContact;
  }

  @Override
  @NonNull
  public MaterialCardView getRoot() {
    return rootView;
  }

  @NonNull
  public static SosDialogBoxBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static SosDialogBoxBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.sos_dialog_box, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static SosDialogBoxBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.number100;
      LinearLayout number100 = ViewBindings.findChildViewById(rootView, id);
      if (number100 == null) {
        break missingId;
      }

      id = R.id.number101;
      LinearLayout number101 = ViewBindings.findChildViewById(rootView, id);
      if (number101 == null) {
        break missingId;
      }

      id = R.id.number102;
      LinearLayout number102 = ViewBindings.findChildViewById(rootView, id);
      if (number102 == null) {
        break missingId;
      }

      id = R.id.number112;
      LinearLayout number112 = ViewBindings.findChildViewById(rootView, id);
      if (number112 == null) {
        break missingId;
      }

      id = R.id.number139;
      LinearLayout number139 = ViewBindings.findChildViewById(rootView, id);
      if (number139 == null) {
        break missingId;
      }

      id = R.id.trustedContName;
      TextView trustedContName = ViewBindings.findChildViewById(rootView, id);
      if (trustedContName == null) {
        break missingId;
      }

      id = R.id.trustedContact;
      LinearLayout trustedContact = ViewBindings.findChildViewById(rootView, id);
      if (trustedContact == null) {
        break missingId;
      }

      return new SosDialogBoxBinding((MaterialCardView) rootView, number100, number101, number102,
          number112, number139, trustedContName, trustedContact);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
