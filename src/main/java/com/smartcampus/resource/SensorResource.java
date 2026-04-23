/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
public class SensorResource {

    // GET /api/v1/sensors  (optional: ?type=CO2)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> list = new ArrayList<>(DataStore.sensors.values());

        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : list) {
                if (s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return Response.ok(filtered).build();
        }
        return Response.ok(list).build();
    }

    // POST /api/v1/sensors  - register a new sensor
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // Validate that the referenced roomId actually exists
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Room with id '" + sensor.getRoomId() + "' does not exist. " +
                "Cannot register sensor."
            );
        }
        if (DataStore.sensors.containsKey(sensor.getId())) {
            return Response.status(409)
                           .entity("{\"message\":\"Sensor already exists\"}").build();
        }
        DataStore.sensors.put(sensor.getId(), sensor);
        // Link sensor ID into the room's list
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        // Prepare an empty readings list for this sensor
        DataStore.readings.put(sensor.getId(), new ArrayList<>());

        return Response.status(201).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}  - get one sensor
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                           .entity("{\"message\":\"Sensor not found\"}").build();
        }
        return Response.ok(sensor).build();
    }

    // Sub-Resource Locator: delegates /api/v1/sensors/{sensorId}/readings
    // to a dedicated SensorReadingResource class
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}

