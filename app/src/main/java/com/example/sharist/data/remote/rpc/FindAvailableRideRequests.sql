CREATE OR REPLACE FUNCTION get_ride_requests_near(
    lat float,
    lng float,
    radius_km float
)
RETURNS SETOF "rideRequest" AS $$
DECLARE
    lat_delta float := radius_km / 111.0;
    lng_delta float := radius_km / (111.0 * COS(RADIANS(lat)));
BEGIN
    RETURN QUERY
    SELECT * FROM "rideRequest"
    WHERE
        date > NOW()
        AND "driverId" IS NULL
        AND (departure->>'latitude')::float  BETWEEN lat - lat_delta AND lat + lat_delta
        AND (departure->>'longitude')::float BETWEEN lng - lng_delta AND lng + lng_delta;
END;
$$ LANGUAGE plpgsql;