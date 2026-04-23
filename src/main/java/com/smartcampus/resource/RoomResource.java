/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
public class RoomResource {

    // GET /api/v1/rooms  - return all rooms
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        List<Room> list = new ArrayList<>(DataStore.rooms.values());
        return Response.ok(list).build();
    }

    // POST /api/v1/rooms  - create a new room
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(400)
                           .entity("{\"message\":\"Room id is required\"}").build();
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(409)
                           .entity("{\"message\":\"Room already exists\"}").build();
        }
        DataStore.rooms.put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId}  - get one room
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                           .entity("{\"message\":\"Room not found\"}").build();
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}  - delete room (only if no sensors)
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            // Idempotent: deleting non-existent room is OK
            return Response.status(404)
                           .entity("{\"message\":\"Room not found\"}").build();
        }
        if (!room.getSensorIds().isEmpty()) {
            // Business rule: cannot delete room with active sensors
            throw new RoomNotEmptyException(
                "Cannot delete room '" + roomId + "': it still has " +
                room.getSensorIds().size() + " sensor(s) assigned to it."
            );
        }
        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }
}

