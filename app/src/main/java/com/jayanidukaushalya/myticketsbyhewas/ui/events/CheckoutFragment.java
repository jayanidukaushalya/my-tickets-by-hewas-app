package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;
import com.jayanidukaushalya.myticketsbyhewas.data.model.PurchaseResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReservationResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.CustomerApiClient;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.CustomerApiClientImpl;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.PurchaseApiClient;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.PurchaseApiClientImpl;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReserveTicketsRequest;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentCheckoutBinding;
import com.jayanidukaushalya.myticketsbyhewas.databinding.ItemCheckoutTicketRowBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;

public class CheckoutFragment extends Fragment {

    private static final int PAYHERE_REQUEST_CODE = 456;

    private FragmentCheckoutBinding binding;
    private PurchaseApiClient purchaseApiClient;
    private CustomerApiClient customerApiClient;
    private final Gson gson = new Gson();

    /** Parsed list of all selected ticket types with quantities. */
    private final List<TicketItem> ticketItems = new ArrayList<>();

    private ReservationResponse pendingReservation;
    private boolean isProcessingPayment = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        purchaseApiClient = PurchaseApiClientImpl.getInstance();
        customerApiClient = CustomerApiClientImpl.getInstance();

        parseArgs();
        setupToolbar();
        buildTicketStub();
        populateContactInputs();
        setupValidationWatchers();
        binding.buttonPayNow.setOnClickListener(v -> onPayNowClicked());
    }

    private void setupValidationWatchers() {
        binding.inputEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.layoutEmail.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        binding.inputPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.layoutPhone.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    // ── Arg parsing ─────────────────────────────────────────────────────────

    private String eventName;
    private String eventImageUrl;
    private String eventDateTime;
    private String eventVenue;

    private void parseArgs() {
        Bundle args = getArguments();
        if (args == null) return;
        
        // Parse event info
        eventName = args.getString("eventName", "");
        eventImageUrl = args.getString("eventImageUrl", "");
        eventDateTime = args.getString("eventDateTime", "");
        eventVenue = args.getString("eventVenue", "");
        
        // Parse tickets
        String json = args.getString("ticketsJson");
        if (json == null || json.isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                TicketItem item = new TicketItem(
                        obj.optString("ticketId"),
                        obj.optString("ticketName"),
                        obj.optString("ticketPrice"),
                        obj.optInt("qty", 0)
                );
                if (!item.ticketId.isEmpty() && item.qty > 0) {
                    ticketItems.add(item);
                }
            }
        } catch (Exception e) {
            // ignore parse errors — caught later in buildTicketStub
        }
    }

    // ── Toolbar ──────────────────────────────────────────────────────────────

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    // ── Ticket stub UI ───────────────────────────────────────────────────────

    private void buildTicketStub() {
        if (ticketItems.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Invalid checkout details", Snackbar.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        // Load event image
        if (eventImageUrl != null && !eventImageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(eventImageUrl)
                    .placeholder(R.drawable.ic_event_placeholder)
                    .centerCrop()
                    .into(binding.imageEventThumbnail);
        }

        // Set event info
        binding.textEventName.setText(eventName != null ? eventName : "");
        binding.textEventDatetime.setText(eventDateTime != null ? eventDateTime : "");
        binding.textEventVenue.setText(eventVenue != null && !eventVenue.isEmpty() ? eventVenue : "");
        binding.textEventVenue.setVisibility(eventVenue != null && !eventVenue.isEmpty() ? View.VISIBLE : View.GONE);

        LinearLayout container = binding.layoutTicketItems;
        container.removeAllViews();

        double grandTotal = 0;

        for (int i = 0; i < ticketItems.size(); i++) {
            TicketItem item = ticketItems.get(i);
            ItemCheckoutTicketRowBinding row = ItemCheckoutTicketRowBinding.inflate(
                    LayoutInflater.from(requireContext()), container, false);

            double unitPrice = parsePrice(item.ticketPrice);
            grandTotal += unitPrice * item.qty;

            row.textItemName.setText(item.ticketName);

            String priceFormatted = unitPrice > 0
                    ? String.format(Locale.US, "LKR %,.2f", unitPrice)
                    : getString(R.string.label_free_ticket);

            row.textItemTotal.setText(String.format(Locale.US, "%d × %s", item.qty, priceFormatted));

            container.addView(row.getRoot());

            // Divider between rows
            if (i < ticketItems.size() - 1) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0x1AFFFFFF); // outline_variant
                container.addView(divider);
            }
        }

        // Grand total in bottom bar
        String totalText = grandTotal > 0
                ? String.format(Locale.US, "LKR %,.0f", grandTotal)
                : getString(R.string.label_free_ticket);
        binding.textTotalPrice.setText(totalText);
        binding.buttonPayNow.setText(getString(R.string.action_pay_total, totalText));
    }

    // ── Contact fields ───────────────────────────────────────────────────────

    private void populateContactInputs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.inputEmail.setEnabled(true);
            binding.inputEmail.setText("");
            binding.inputPhone.setText("");
            return;
        }

        binding.inputEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        binding.inputEmail.setEnabled(false);

        user.getIdToken(false).addOnSuccessListener(tokenResult -> {
            String token = tokenResult.getToken();
            if (token == null || token.isEmpty()) {
                binding.inputPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                return;
            }
            customerApiClient.getCustomerByUid(token, new CustomerApiClient.CustomerCallback() {
                @Override
                public void onSuccess(Customer customer) {
                    String phone = customer.getPhone();
                    if (phone == null || phone.trim().isEmpty()) phone = user.getPhoneNumber();
                    binding.inputPhone.setText(phone != null ? phone : "");
                }
                @Override
                public void onFailure(String errorMessage) {
                    binding.inputPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                }
            });
        }).addOnFailureListener(e -> binding.inputPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : ""));
    }

    private void onPayNowClicked() {
        if (isProcessingPayment) return;
        clearErrors();

        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String phone = binding.inputPhone.getText() != null ? binding.inputPhone.getText().toString().trim() : "";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.layoutEmail.setError(getString(R.string.error_invalid_email));
                return;
            }
        }
        if (phone.isEmpty()) {
            binding.layoutPhone.setError(getString(R.string.error_phone_required));
            return;
        }

        BuyerInfo buyerInfo = buildBuyerInfo(user, email, phone);
        reserveAllAndStartPayment(buyerInfo, user);
    }

    private void clearErrors() {
        binding.layoutEmail.setError(null);
        binding.layoutPhone.setError(null);
    }

    private BuyerInfo buildBuyerInfo(@Nullable FirebaseUser user, String emailInput, String phone) {
        if (user == null) return new BuyerInfo("Guest", "User", emailInput, phone);
        String firstName = "Guest", lastName = "User";
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            String[] parts = user.getDisplayName().trim().split("\\s+");
            firstName = parts[0];
            if (parts.length > 1) lastName = user.getDisplayName().trim().substring(firstName.length()).trim();
        }
        String email = user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : emailInput;
        return new BuyerInfo(firstName, lastName, email, phone);
    }

    // ── Reservation: single bulk reserve call ───────────────────────────────

    private void reserveAllAndStartPayment(BuyerInfo buyerInfo, @Nullable FirebaseUser user) {
        setLoading(true);

        List<ReserveTicketsRequest.Item> items = new ArrayList<>();
        for (TicketItem item : ticketItems) {
            items.add(new ReserveTicketsRequest.Item(item.ticketId, item.qty));
        }

        if (user != null) {
            user.getIdToken(false)
                    .addOnSuccessListener(tokenResult ->
                            purchaseApiClient.reserveTicketsBulk(
                                    items,
                                    null,
                                    null,
                                    null,
                                    buyerInfo.phone,
                                    tokenResult.getToken(),
                                    new PurchaseApiClient.ReservationCallback() {
                                        @Override
                                        public void onSuccess(ReservationResponse reservation) {
                                            pendingReservation = reservation;
                                            startPayHerePayment(buyerInfo, reservation);
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            setLoading(false);
                                            showErrorSnackbar(errorMessage);
                                        }
                                    }
                            )
                    )
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        showErrorSnackbar("Authentication failed. Please sign in again.");
                    });
        } else {
            purchaseApiClient.reserveTicketsBulk(
                    items,
                    buyerInfo.email,
                    buyerInfo.firstName,
                    buyerInfo.lastName,
                    buyerInfo.phone,
                    null,
                    new PurchaseApiClient.ReservationCallback() {
                        @Override
                        public void onSuccess(ReservationResponse reservation) {
                            pendingReservation = reservation;
                            startPayHerePayment(buyerInfo, reservation);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            setLoading(false);
                            showErrorSnackbar(errorMessage);
                        }
                    }
            );
        }
    }

    // ── PayHere ───────────────────────────────────────────────────────────────

    private void startPayHerePayment(BuyerInfo buyerInfo, ReservationResponse reservation) {
        if (reservation == null || reservation.getPayment() == null) {
            setLoading(false);
            showErrorSnackbar("Payment configuration is missing");
            return;
        }

        ReservationResponse.PaymentInfo payment = reservation.getPayment();

        // Use the amount from the API response (already calculated on backend)
        double amount = 0;
        try {
            amount = Double.parseDouble(payment.getAmount());
        } catch (NumberFormatException e) {
            setLoading(false);
            showErrorSnackbar("Invalid payment amount");
            return;
        }

        String orderSessionId = reservation.getOrderSessionId();

        InitRequest req = new InitRequest();
        req.setMerchantId(payment.getMerchantId());
        req.setMerchantSecret(payment.getMerchantSecret());
        req.setCurrency(payment.getCurrency());
        req.setAmount(amount);
        req.setOrderId(payment.getOrderId() != null ? payment.getOrderId() : UUID.randomUUID().toString());
        req.setItemsDescription(buildItemsDescription());
        req.setNotifyUrl(payment.getNotifyUrl());
        if (orderSessionId != null && !orderSessionId.isEmpty()) {
            req.setCustom1(orderSessionId);
        }

        req.getCustomer().setFirstName(buyerInfo.firstName);
        req.getCustomer().setLastName(buyerInfo.lastName);
        req.getCustomer().setEmail(buyerInfo.email);
        req.getCustomer().setPhone(buyerInfo.phone);
        req.getCustomer().getAddress().setAddress("N/A");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(requireActivity(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(payment.isSandbox() ? PHConfigs.SANDBOX_URL : PHConfigs.LIVE_URL);
        startActivityForResult(intent, PAYHERE_REQUEST_CODE);
    }

    private String buildItemsDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ticketItems.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(ticketItems.get(i).qty).append("x ").append(ticketItems.get(i).ticketName);
        }
        return sb.toString();
    }

    // ── PayHere result ────────────────────────────────────────────────────────

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PAYHERE_REQUEST_CODE) return;

        if (data == null || !data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            setLoading(false);
            showErrorSnackbar("Payment cancelled");
            return;
        }

        PHResponse response = (PHResponse) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
        if (response == null) {
            setLoading(false);
            showErrorSnackbar("Payment cancelled");
            return;
        }

        String responseJson = gson.toJson(response);
        String responseJsonUpper = responseJson.toUpperCase(Locale.US);
        boolean paymentSuccess = responseJsonUpper.contains("SUCCESS")
                || responseJsonUpper.contains("\"CODE\":1")
                || responseJsonUpper.contains("\"CODE\":2");

        if (!paymentSuccess) {
            setLoading(false);
            String errorMsg = "Payment was not completed";
            Object responseData = response.getData();
            if (responseData != null && !responseData.toString().isEmpty()) {
                errorMsg = responseData.toString();
            }
            showErrorSnackbar(errorMsg);
            return;
        }

        if (pendingReservation == null) {
            setLoading(false);
            showErrorSnackbar("Purchase session is missing");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(false)
                    .addOnSuccessListener(tokenResult -> confirmAllPurchases(tokenResult.getToken()))
                    .addOnFailureListener(e -> { setLoading(false); showErrorSnackbar("Failed to confirm purchase"); });
        } else {
            confirmAllPurchases(null);
        }
    }

    private void confirmAllPurchases(@Nullable String token) {
        String orderSessionId = pendingReservation.getOrderSessionId();
        if (orderSessionId == null || orderSessionId.isEmpty()) {
            setLoading(false);
            showErrorSnackbar("Purchase session is missing");
            return;
        }

        purchaseApiClient.confirmPurchase(orderSessionId, token, new PurchaseApiClient.PurchaseCallback() {
            @Override
            public void onSuccess(PurchaseResponse purchase) {
                String firstTicketId = null;
                if (purchase != null && purchase.getPurchases() != null && !purchase.getPurchases().isEmpty()) {
                    firstTicketId = purchase.getPurchases().get(0).getTicketId();
                }
                onAllConfirmed(firstTicketId);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showErrorSnackbar(errorMessage);
            }
        });
    }

    private void onAllConfirmed(@Nullable String newlyPurchasedTicketId) {
        setLoading(false);
        showSuccessSnackbar("Payment successful! Tickets purchased.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && newlyPurchasedTicketId != null) {
            // Navigate directly to the new ticket detail screen
            Bundle args = new Bundle();
            args.putString("ticketId", newlyPurchasedTicketId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_checkout_to_ticket_detail, args);
        } else if (user != null) {
            // Fallback: Navigate to profile list if ID missing
            NavHostFragment.findNavController(this).navigate(R.id.nav_profile);
        } else {
            NavHostFragment.findNavController(this).navigateUp();
        }
    }

    private void showSuccessSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary));
        snackbar.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.on_primary));
        snackbar.show();
    }

    private void showErrorSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.error));
        snackbar.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.on_error));
        snackbar.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        isProcessingPayment = loading;
        binding.buttonPayNow.setEnabled(!loading);
        if (loading) {
            binding.buttonPayNow.setText("Processing…");
        } else {
            buildTicketStub(); // restore button text
        }
    }

    private double parsePrice(@Nullable String price) {
        if (price == null) return 0;
        try { return Double.parseDouble(price); } catch (NumberFormatException e) { return 0; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ── Data classes ──────────────────────────────────────────────────────────

    private static final class TicketItem {
        final String ticketId;
        final String ticketName;
        final String ticketPrice;
        final int qty;
        TicketItem(String ticketId, String ticketName, String ticketPrice, int qty) {
            this.ticketId = ticketId;
            this.ticketName = ticketName;
            this.ticketPrice = ticketPrice;
            this.qty = qty;
        }
    }

    private static final class BuyerInfo {
        final String firstName, lastName, email, phone;
        BuyerInfo(String firstName, String lastName, String email, String phone) {
            this.firstName = firstName; this.lastName = lastName;
            this.email = email; this.phone = phone;
        }
    }
}
