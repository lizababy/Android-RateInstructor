package liza.com.rateinstructor;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InstructorDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InstructorDialogFragment extends DialogFragment {

    private static final String ARG_MODE = "mode";

    private String mDialogMode;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment RateMeDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static InstructorDialogFragment newInstance(String mode) {
        InstructorDialogFragment fragment = new InstructorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    public InstructorDialogFragment() {
        // Required empty public constructor
    }
    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface InstructorDialogListener {
        public void onDialogPositiveClick(String dialogMode, String data);
        public void onDialogNegativeClick();
    }
    // Use this instance of the interface to deliver action events
    InstructorDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (InstructorDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if (getArguments() != null) {
            mDialogMode = getArguments().getString(ARG_MODE);
        }
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.mipmap.ic_launcher);
        switch (mDialogMode){
            case "0":
                builder.setMessage(R.string.alertMessg)
                        .setPositiveButton(R.string.ok,null);
                break;

            case "1"://Dialog for Rating
                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                final View ratingView = inflater.inflate(R.layout.add_rating, null);
                builder.setView(ratingView)
                        .setTitle(R.string.rateMeTitle)
                        .setMessage(R.string.RateMeMessage)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                RatingBar rating_bar = (RatingBar)ratingView.findViewById(R.id.ratingBar);
                                String total_rating = Integer.toString((int)rating_bar.getRating());
                                mListener.onDialogPositiveClick(mDialogMode, total_rating);
                            }
                        })
                        .setNegativeButton(R.string.cancel,null);
                break;
            case "2"://Dialog for add comment

                final View commentView = inflater.inflate(R.layout.add_comment, null);
                builder.setView(commentView)
                        .setTitle(R.string.commentMeTitle)
                        .setMessage(R.string.commentMeMessage)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                EditText comment_text=(EditText)commentView.findViewById(R.id.comment_editText);
                                String comment = comment_text.getText().toString();
                                mListener.onDialogPositiveClick(mDialogMode,comment);
                            }
                        })
                        .setNegativeButton(R.string.cancel,null);

                break;
            case "3"://Dialog to read from Database
                builder.setMessage(R.string.alertMessg2)
                        .setTitle(R.string.dbReadTitle)
                        .setPositiveButton(R.string.proceed,new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogPositiveClick("","");
                            }
                        })
                        .setNegativeButton(R.string.quit,new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogNegativeClick();
                            }
                        });
                break;
            case "4"://Dialog showing details not updated.if ok go to previous
                builder.setMessage(R.string.noUpdates)
                        .setNegativeButton(R.string.ok,new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick();
                    }
                });
                break;

            default: //Dialog showing connect to network and restart.if ok quit
                builder.setMessage(R.string.quiting)
                        .setNegativeButton(R.string.ok,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id) {
                    mListener.onDialogNegativeClick();
                }
            });


        }

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
