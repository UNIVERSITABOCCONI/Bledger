package it.bocconi.bledger.feature.auth.enums;

/**
 * Defines the base API paths used by the application and interpreted by the
 * authentication and authorization filter.
 *
 * <p>
 * Each path represents a logical API area with specific access rules.
 * Authorization is enforced through path matching combined with company
 * type checks and contextual permissions.
 * </p>
 *
 * <p>
 * The semantics of these paths reflect business responsibilities rather than
 * technical layers, and are used to keep the PoC security model simple and explicit.
 * </p>
 */
public interface BLPaths {

    /**
     * Public APIs.
     *
     * <p>
     * Endpoints under this path are fully accessible without authentication
     * and without any application context.
     * </p>
     */
    String PUBLIC = "/api/v1/public";

    /**
     * Common APIs.
     *
     * <p>
     * Shared endpoints that require an authenticated application context
     * (i.e. a valid {@code BL_COMPANY_ID} cookie), but are not restricted
     * to a specific business role.
     * </p>
     */
    String COMMON = "/api/v1/common";

    /**
     * Business Unit (BU) APIs.
     *
     * <p>
     * Endpoints intended for internal business unit operations.
     * Accessible only to non-TPA companies.
     * </p>
     */
    String BU = "/api/v1/bu";

    /**
     * Business Network Administrator (BNA) APIs.
     *
     * <p>
     * Endpoints that allow management and administration of business networks.
     * Access is restricted to non-TPA companies and may require additional
     * network-level administrative privileges depending on the operation.
     * </p>
     */
    String BNA = "/api/v1/bna";

    /**
     * Third-Party Auditors (TPA) APIs.
     *
     * <p>
     * Endpoints reserved exclusively for companies acting as Third-Party
     * Auditors.
     * Non-TPA companies are explicitly forbidden from accessing these APIs.
     * </p>
     */
    String TPA = "/api/v1/tpa";

    /**
     * Administrative (AM) APIs.
     *
     * <p>
     * System-level administrative endpoints protected via Basic Authentication
     * using credentials defined in application configuration.
     * These APIs are independent from the company application context.
     * </p>
     */
    String AM = "/api/v1/am";
}

