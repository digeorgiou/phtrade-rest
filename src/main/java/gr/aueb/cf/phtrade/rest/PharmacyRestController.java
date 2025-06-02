package gr.aueb.cf.phtrade.rest;


import gr.aueb.cf.phtrade.core.exceptions.*;
import gr.aueb.cf.phtrade.dto.*;
import gr.aueb.cf.phtrade.mapper.Mapper;
import gr.aueb.cf.phtrade.model.Pharmacy;
import gr.aueb.cf.phtrade.service.IPharmacyService;
import gr.aueb.cf.phtrade.validator.ValidatorUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@ApplicationScoped
@Path("/pharmacies")
public class PharmacyRestController {

    private final IPharmacyService pharmacyService;

    @Inject
    public PharmacyRestController(IPharmacyService pharmacyService){
        this.pharmacyService = pharmacyService;
    }


    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON) //δινω τι ειδους payload παιρνει
    // (JSON, XML κτλ)
    @Produces(MediaType.APPLICATION_JSON) //δινω τι ειδους δεδομενα στελνει πισω
    public Response addPharmacy(PharmacyInsertDTO insertDTO,
                                @Context UriInfo uriInfo) throws EntityInvalidArgumentException, EntityNotFoundException, EntityAlreadyExistsException
    , AppServerException {

        List<String> errors = ValidatorUtil.validateDTO(insertDTO);

        if(!errors.isEmpty()){
            throw new EntityInvalidArgumentException("Pharmacy" ,
                    String.join("\n", errors));
        }

        PharmacyReadOnlyDTO readOnlyDTO = pharmacyService.createPharmacy(insertDTO);

        //γινεται συνενωση του absolute path με το νεο id. /pharmacies/id
        URI newResourceUri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(readOnlyDTO.id()))
                .build();


        //το created δινει κωδικο 201 και ταυτοχρονα περναει και στον header
        // το URI
        return Response.created(newResourceUri).entity(readOnlyDTO).build();

    }

    @PUT
    @Path("/{pharmacyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePharmacy(@PathParam("pharmacyId") Long pharmacyId,
                                  PharmacyUpdateDTO updateDTO) throws EntityInvalidArgumentException, EntityNotFoundException,
            EntityAlreadyExistsException, AppServerException, EntityNotAuthorizedException {

        List<String> errors = ValidatorUtil.validateDTO(updateDTO);

        if(!errors.isEmpty()){
            throw new EntityInvalidArgumentException("Pharmacy" ,
                    String.join("\n", errors));
        }

        PharmacyReadOnlyDTO readOnlyDTO =
                pharmacyService.updatePharmacy(updateDTO);

        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO) // το readonly dto γινεται αυτοματα json
                // μεσω του dependency του jackson
                .build();

    }

    @DELETE
    @Path("/{pharmacyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePharmacy(@PathParam("pharmacyId") Long pharmacyId,
                                   @PathParam("userId") Long deleterUserId) throws EntityNotFoundException{


        PharmacyReadOnlyDTO readOnlyDTO =
                pharmacyService.getPharmacyById(pharmacyId);
        pharmacyService.deletePharmacy(pharmacyId, deleterUserId);

        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }

    @GET
    @Path("/{pharmacyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPharmacy(@PathParam("pharmacyId") Long pharmacyId) throws EntityNotFoundException{
        PharmacyReadOnlyDTO readOnlyDTO =
                pharmacyService.getPharmacyById(pharmacyId);
        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }

    //Το get All Pharmacies μπορει να γινει ειτε με pagination, ειτε χωρις

    @GET
    @Path("/filtered")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFiltered(@QueryParam("name") @DefaultValue("") String name,
                                @QueryParam("username")@DefaultValue("") String username){

        PharmacyFiltersDTO filtersDTO = new PharmacyFiltersDTO(name, username);
        Map<String, Object> criteria =
                Mapper.mapPharmacyFiltersToCriteria(filtersDTO);

        List<PharmacyReadOnlyDTO> readOnlyDTOS =
                pharmacyService.getPharmaciesByCriteria(criteria);
        return Response.status(Response.Status.OK)
                .entity(readOnlyDTOS)
                .build();
    }
    @GET
    @Path("/paginated")
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResult<PharmacyReadOnlyDTO> getFilteredPaginated (@QueryParam("name") @DefaultValue("") String name,
                                                                      @QueryParam("username")@DefaultValue("") String username,
                                                                      @QueryParam("page")@DefaultValue("0") Integer page,
                                                                      @QueryParam("size")@DefaultValue("10") Integer size)
    throws EntityInvalidArgumentException{

        PharmacyFiltersDTO filtersDTO = new PharmacyFiltersDTO(name, username);
        Map<String, Object> criteria;

        criteria = Mapper.mapPharmacyFiltersToCriteria(filtersDTO);

        if(page < 0) throw new EntityInvalidArgumentException(
                "PageInvalidNumber","Invalid page number");
        if(size <= 0) throw new EntityInvalidArgumentException(
                "SizeInvalidNumber", "Invalid size number"
        );

        List<PharmacyReadOnlyDTO> readOnlyDTOS =
                pharmacyService.getPharmaciesByCriteriaPaginated(criteria, page, size);

        long totalItems =
                pharmacyService.getPharmaciesCountByCriteria(criteria);

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
