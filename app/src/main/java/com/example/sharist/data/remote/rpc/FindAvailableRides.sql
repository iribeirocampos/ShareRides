CREATE OR REPLACE FUNCTION get_rides_near(
    lat float,
    lng float,
    radius_km float
)
RETURNS SETOF ride AS $$
DECLARE
    lat_delta float := radius_km / 111.0;
    lng_delta float := radius_km / (111.0 * COS(RADIANS(lat)));
BEGIN
    RETURN QUERY
    SELECT * FROM ride
    WHERE
        "availableSeats" > 0
        AND "departureDateTime" > NOW()
        AND (departure->>'latitude')::float  BETWEEN lat - lat_delta AND lat + lat_delta
        AND (departure->>'longitude')::float BETWEEN lng - lng_delta AND lng + lng_delta;
END;
$$ LANGUAGE plpgsql;