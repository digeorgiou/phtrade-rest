package gr.aueb.cf.phtrade.rest;

import gr.aueb.cf.phtrade.core.exceptions.*;
import gr.aueb.cf.phtrade.dto.*;
import gr.aueb.cf.phtrade.mapper.Mapper;
import gr.aueb.cf.phtrade.service.IUserService;
import gr.aueb.cf.phtrade.validator.ValidatorUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Path("/users")
public class UserRestController {
    private final IUserService userService;

    @Inject
    public UserRestController(IUserService userService){
        this.userService = userService;
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserInsertDTO insertDTO,
                            @Context UriInfo uriInfo) throws EntityInvalidArgumentException,
            EntityAlreadyExistsException, AppServerException {

        List<String> errors = ValidatorUtil.validateDTO(insertDTO);

        if(!errors.isEmpty()){
            throw new EntityInvalidArgumentException("User" ,
                    String.join("\n", errors));
        }

        UserReadOnlyDTO readOnlyDTO = userService.insertUser(insertDTO);

        //γινεται συνενωση του absolute path με το νεο id. /users/id
        URI newResourceUri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(readOnlyDTO.id()))
                .build();

        //το created δινει κωδικο 201 και ταυτοχρονα περναει και στον header
        // το URI
        return Response.created(newResourceUri).entity(readOnlyDTO).build();
    }

    @PUT
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePharmacy(@PathParam("userId") Long pharmacyId,
                                   UserUpdateDTO updateDTO) throws EntityInvalidArgumentException, EntityNotFoundException,
            EntityAlreadyExistsException, AppServerException, EntityNotAuthorizedException {

        List<String> errors = ValidatorUtil.validateDTO(updateDTO);

        if(!errors.isEmpty()){
            throw new EntityInvalidArgumentException("User" ,
                    String.join("\n", errors));
        }

        UserReadOnlyDTO readOnlyDTO =
                userService.updateUser(updateDTO);

        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO) // το readonly dto γινεται αυτοματα json
                // μεσω του dependency του jackson
                .build();

    }

    @DELETE
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePharmacy(@PathParam("userId") Long userId,
                                   @PathParam("deleterUserId") Long deleterUserId)
            throws EntityNotFoundException, EntityNotAuthorizedException{


        UserReadOnlyDTO readOnlyDTO = userService.getUserById(userId);

        userService.deleteUser(userId, deleterUserId);

        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPharmacy(@PathParam("userId") Long userId)
            throws EntityNotFoundException{

        UserReadOnlyDTO readOnlyDTO =
                userService.getUserById(userId);
        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }

    //Το get All Pharmacies μπορει να γινει ειτε με pagination, ειτε χωρις

    @GET
    @Path("/filtered")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFiltered(@QueryParam("username") @DefaultValue("") String username,
                                @QueryParam("email")@DefaultValue("") String email){

        UserFiltersDTO filtersDTO = new UserFiltersDTO(username, email);
        Map<String, Object> criteria =
                Mapper.mapUserFiltersToCriteria(filtersDTO);

        List<UserReadOnlyDTO> readOnlyDTOS =
                userService.getUsersByCriteria(criteria);
        return Response.status(Response.Status.OK)
                .entity(readOnlyDTOS)
                .build();
    }

    @GET
    @Path("/paginated")
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResult<UserReadOnlyDTO> getFilteredPaginated (@QueryParam("username") @DefaultValue("") String username,
                                                                      @QueryParam("email")@DefaultValue("") String email,
                                                                      @QueryParam("page")@DefaultValue("0") Integer page,
                                                                      @QueryParam("size")@DefaultValue("10") Integer size)
            throws EntityInvalidArgumentException{

        UserFiltersDTO filtersDTO = new UserFiltersDTO(username, email);
        Map<String, Object> criteria;

        criteria = Mapper.mapUserFiltersToCriteria(filtersDTO);

        if(page < 0) throw new EntityInvalidArgumentException(
                "PageInvalidNumber","Invalid page number");
        if(size <= 0) throw new EntityInvalidArgumentException(
                "SizeInvalidNumber", "Invalid size number"
        );

        List<UserReadOnlyDTO> readOnlyDTOS =
                userService.getUsersByCriteriaPaginated(criteria, page, size);

        long totalItems =
                userService.getUsersCountByCriteria(criteria);

        int totalPages = (int) Math.ceil((double) totalItems / size);

        return new PaginatedResult<>(
                readOnlyDTOS,
                page,
                size,
                totalPages,
                totalItems
        );
    }

}
