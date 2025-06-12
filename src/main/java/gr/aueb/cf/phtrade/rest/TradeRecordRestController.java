package gr.aueb.cf.phtrade.rest;

import gr.aueb.cf.phtrade.core.exceptions.*;
import gr.aueb.cf.phtrade.dto.*;
import gr.aueb.cf.phtrade.mapper.Mapper;
import gr.aueb.cf.phtrade.service.IPharmacyService;
import gr.aueb.cf.phtrade.service.ITradeRecordService;
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
@Path("/records")
public class TradeRecordRestController {

    private final ITradeRecordService recordService;

    @Inject
    public TradeRecordRestController(ITradeRecordService recordService) {
        this.recordService = recordService;
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTradeRecord(TradeRecordInsertDTO insertDTO,
                                   @Context UriInfo uriInfo) throws EntityInvalidArgumentException, EntityAlreadyExistsException,
            EntityNotFoundException, AppServerException, EntityNotAuthorizedException {

        List<String> errors = ValidatorUtil.validateDTO(insertDTO);

        if(!errors.isEmpty()){
            throw new EntityInvalidArgumentException("TradeRecord",
                    String.join("\n", errors));
        }

        TradeRecordReadOnlyDTO readOnlyDTO = recordService.create(insertDTO);

        URI newResourceUri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(readOnlyDTO.id()))
                .build();

        return Response.created(newResourceUri).entity(readOnlyDTO).build();
    }

    @PUT
    @Path("/{recordId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTradeRecord(@PathParam("recordId") Long recordId,
                                      TradeRecordUpdateDTO updateDTO) throws EntityInvalidArgumentException,
            EntityNotFoundException, EntityAlreadyExistsException,
            AppServerException, EntityNotAuthorizedException {

        List<String> errors = ValidatorUtil.validateDTO(updateDTO);

        if(!errors.isEmpty()){
            throw new EntityInvalidArgumentException("TradeRecord",
                    String.join("\n", errors));
        }

        TradeRecordReadOnlyDTO readOnlyDTO =
                recordService.update(updateDTO);

        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }

    @DELETE
    @Path("/{recordId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTradeRecord(@PathParam("recordId") Long recordId,
                                   @PathParam("userId") Long deleterUserId) throws EntityNotFoundException,
            EntityNotAuthorizedException{

        TradeRecordReadOnlyDTO readOnlyDTO = recordService.getById(recordId);

        recordService.delete(recordId, deleterUserId);

        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }

    @GET
    @Path("/{recordId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTradeRecord(@PathParam("recordId") Long recordId) throws EntityNotFoundException{
        TradeRecordReadOnlyDTO readOnlyDTO =
                recordService.getById(recordId);

        return Response.status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }

    public Response getFiltered(@QueryParam("description") @DefaultValue("") String description){
        TradeRecordFiltersDTO filtersDTO =
                new TradeRecordFiltersDTO(description);
        Map<String, Object> criteria =
                Mapper.mapRecordFiltersToCriteria(filtersDTO);

        List<TradeRecordReadOnlyDTO> readOnlyDTOS =
                recordService.getTradeRecordsByCriteria(criteria);

        return Response.status(Response.Status.OK)
                .entity(readOnlyDTOS)
                .build();
    }

    @GET
    @Path("/paginated")
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResult<TradeRecordReadOnlyDTO> getFilteredPaginated (@QueryParam("description") @DefaultValue("") String description,
                                                                      @QueryParam("page")@DefaultValue("0") Integer page,
                                                                      @QueryParam("size")@DefaultValue("10") Integer size)
            throws EntityInvalidArgumentException {

        TradeRecordFiltersDTO filtersDTO =
                new TradeRecordFiltersDTO(description);
        Map<String, Object> criteria =
                Mapper.mapRecordFiltersToCriteria(filtersDTO);

        if(page < 0) throw new EntityInvalidArgumentException(
                "PageInvalidNumber","Invalid page number");
        if(size <= 0) throw new EntityInvalidArgumentException(
                "SizeInvalidNumber", "Invalid size number"
        );

        List<TradeRecordReadOnlyDTO> readOnlyDTOS =
                recordService.getTradeRecordsByCriteriaPaginated(criteria,
                        page, size);

        long totalItems =
                recordService.getTradeRecordsCountByCriteria(criteria);

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
