package com.example.sharkpool_orbital_2019;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Vector;

public class OngoingRequestArrayAdaptor extends RecyclerView.Adapter<OngoingRequestArrayAdaptor.MyViewHolder>{
    static Vector<BorrowRequest> mDataset;
    String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    protected static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView borrowerName;
        public TextView creditVal;
        public TextView itemName;
        public TextView itemType;
        public TextView creationDate;
        public TextView reqStatus;

        public MyViewHolder(CardView v) {
            super(v);

            itemName = v.findViewById(R.id.ItemName);
            itemType = v.findViewById(R.id.ItemType);
            borrowerName = v.findViewById(R.id.borrowerName);
            creditVal = v.findViewById(R.id.creditVal);
            creationDate = v.findViewById(R.id.creationDate);
            reqStatus = v.findViewById(R.id.reqStatus);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Get the item clicked
            // For this example, I'm assuming your data source is of type `List<MyObject>`
            BorrowRequest myObject = mDataset.get(getAdapterPosition());
            // Then you can do any actions on it, for example:
            Intent intent = new Intent(v.getContext(), BRview.class);
            intent.putExtra("initiator", myObject.getRequestID());
            v.getContext().startActivity(intent);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public OngoingRequestArrayAdaptor(Vector<BorrowRequest> myDataset) {
        mDataset = myDataset;
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public OngoingRequestArrayAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                      int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ongoingrequest_layout, parent, false);

        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final OngoingRequestArrayAdaptor.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        CardView temp = (CardView) holder.itemView;
        holder.itemName.setText(mDataset.elementAt(position).getItemName());
        holder.itemType.setText(mDataset.elementAt(position).getItemType());
        holder.creationDate.append(mDataset.elementAt(position).getCreatedDate().toString());

        Integer borrowerCredits =  mDataset.elementAt(position).getCreditValue();
        holder.creditVal.setText(borrowerCredits.toString());

        String borrowerName = mDataset.elementAt(position).getBorrowerName();
        String lenderName = mDataset.elementAt(position).getLenderName();

        if (mDataset.elementAt(position).getLenderUID().equals(userUID)){
            holder.borrowerName.setText("Lending to: ".concat(borrowerName));
        }
        else {
            if (lenderName.isEmpty()){
                holder.borrowerName.append("-");
            }
            else{
                holder.borrowerName.append(lenderName);
            }
        }
        String currStatus = mDataset.elementAt(position).getStatus();
        boolean disputed = mDataset.elementAt(position).isDispute();

        if (disputed){
            currStatus = currStatus.concat((" (Under Dispute)"));
        }
        holder.reqStatus.setText(currStatus);

        switch(currStatus){
            case "Closed":
                temp.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.coral_light_blue));
                break;

            case "Lent/Borrowed":
                temp.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.coral_light_yellow));
                break;

            case "Completed":
                break;

            case "Open":
                temp.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.pale_green));
                break;

            default: //Open
                temp.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.coral_light_red));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}