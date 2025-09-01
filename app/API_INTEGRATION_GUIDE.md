# FitMe Mobile - API Integration Guide

## Overview
This guide explains how the mobile app integrates with the backend API for membership upgrades.

## API Endpoints

### 1. Upgrade Membership
- **Endpoint**: `POST /api/payment/upgrade-membership`
- **Purpose**: Upgrades an existing user's membership plan
- **Used in**: `PaymentMethodActivity.handleMembershipUpgrade()`

### 2. Get Current Membership
- **Endpoint**: `GET /api/payment/membership/{user_id}`
- **Purpose**: Retrieves current active membership details
- **Used in**: `MembershipDetailsFragment.fetchMembershipDetails()`

### 3. Get Available Plans
- **Endpoint**: `GET /api/payment/get-plan`
- **Purpose**: Retrieves all available membership plans
- **Used in**: `MembershipUpgradeActivity.fetchPlans()`

## Integration Status

### ✅ Completed
1. **API Interface**: `Payments.kt` interface with all endpoints
2. **Data Models**: Request/Response models for membership upgrade
3. **RetrofitClient**: Configured with proper base URL
4. **PaymentMethodActivity**: Handles both registration and upgrade flows
5. **MembershipDetailsFragment**: Uses new API with fallback to old API
6. **MembershipUpgradeActivity**: Fetches plans and navigates to payment
7. **Error Handling**: Comprehensive error handling and logging
8. **Testing Utilities**: API test helper for development

### 🔧 Configuration

#### Base URL Configuration
The app uses the following base URLs (configured in `RetrofitClient.kt`):
- **Local Development**: `https://6aa0ee346c21.ngrok-free.app/api/`
- **Production**: `https://fitmegym.com/api/`

To change the environment, modify `USE_LOCAL` in `RetrofitClient.kt`:
```kotlin
private const val USE_LOCAL = true  // Set to false for production
```

## Testing the Integration

### 1. API Testing
Use the built-in test utility by long-pressing the "Change Plan" button in `MembershipDetailsFragment`:
- Long press triggers API tests
- Check Android logs for test results
- Tests include: get current membership, get plans

### 2. Manual Testing Flow
1. **View Membership**: Open `MembershipDetailsFragment`
2. **Change Plan**: Tap "Change Plan" button
3. **Select Plan**: Choose a new plan in `MembershipUpgradeActivity`
4. **Payment**: Complete payment in `PaymentMethodActivity`
5. **Success**: Navigate to success screen

### 3. Log Monitoring
Monitor these log tags for debugging:
- `UPGRADE_DEBUG`: Membership upgrade process
- `API_TEST`: API testing results
- `MembershipDetails`: Membership loading
- `FitMeBaseURL`: Base URL configuration

## Data Flow

### Membership Upgrade Flow
```
MembershipDetailsFragment
    ↓ (Change Plan button)
MembershipUpgradeActivity
    ↓ (Select plan + Continue)
PaymentMethodActivity
    ↓ (handleMembershipUpgrade)
Backend API: POST /api/payment/upgrade-membership
    ↓ (Success response)
Success/Pending Activity
```

### Membership Details Flow
```
MembershipDetailsFragment
    ↓ (fetchMembershipDetails)
Backend API: GET /api/payment/membership/{user_id}
    ↓ (Success response)
Update UI with membership data
```

## Error Handling

### Network Errors
- Connection timeouts
- Network unavailable
- Server errors (5xx)

### API Errors
- Invalid user ID (404)
- Plan not found (404)
- Missing required fields (400)
- Payment method validation (400)

### Fallback Strategy
- Primary: New API endpoint
- Fallback: Old API endpoint
- Error State: Show "Unable to load"

## Request/Response Examples

### Upgrade Membership Request
```json
{
  "user_id": 123,
  "plan": "Pro Plan",
  "price": 1500,
  "payment_method": "GCASH",
  "gcash_number": "09123456789",
  "gcash_name": "John Doe"
}
```

### Upgrade Membership Response
```json
{
  "success": true,
  "message": "Membership upgraded successfully",
  "data": {
    "user_id": 123,
    "plan": "Pro Plan",
    "price": 1500,
    "payment_method": "GCASH",
    "status": "active",
    "end_date": "2024-12-31",
    "created_at": "2024-01-01T10:00:00.000Z"
  }
}
```

### Get Current Membership Response
```json
{
  "success": true,
  "data": {
    "id": 456,
    "user_id": 123,
    "user_name": "John Doe",
    "user_email": "john@example.com",
    "plan": "Pro Plan",
    "price": 1500,
    "end_date": "2024-12-31",
    "payment_method": "GCASH",
    "status": "active",
    "is_expired": false
  }
}
```

## Troubleshooting

### Common Issues

1. **API Not Responding**
   - Check base URL configuration
   - Verify network connectivity
   - Check backend server status

2. **Authentication Errors**
   - Verify user is logged in
   - Check user ID in SharedPreferences
   - Ensure valid session

3. **Plan Not Found**
   - Verify plan exists in database
   - Check plan is active
   - Ensure correct plan name

4. **Payment Method Errors**
   - Validate required fields for payment method
   - Check payment method format
   - Verify payment details

### Debug Steps

1. **Enable Logging**
   ```kotlin
   Log.d("UPGRADE_DEBUG", "Request: ${Gson().toJson(request)}")
   ```

2. **Check Network**
   - Test with cURL or Postman
   - Verify backend endpoints
   - Check server logs

3. **Validate Data**
   - Check user ID exists
   - Verify plan data
   - Validate payment details

## Production Deployment

### Before Going Live
1. Update `USE_LOCAL = false` in `RetrofitClient.kt`
2. Verify production base URL
3. Test all payment methods
4. Validate error handling
5. Remove debug logging (optional)

### Monitoring
- Monitor API response times
- Track error rates
- Log payment success/failure rates
- Monitor user upgrade patterns

## Support

For issues or questions:
1. Check Android logs for error details
2. Verify backend API status
3. Test with API test utility
4. Review this integration guide
