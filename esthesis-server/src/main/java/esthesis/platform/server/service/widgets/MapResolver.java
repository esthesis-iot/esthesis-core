package esthesis.platform.server.service.widgets;

import com.eurodyn.qlack.common.exception.QMismatchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.config.AppSettings.Setting.Device;
import esthesis.platform.server.dto.DeviceCoordinates;
import esthesis.platform.server.dto.DevicePageDTO;
import esthesis.platform.server.model.DashboardWidget;
import esthesis.platform.server.service.DeviceService;
import esthesis.platform.server.service.SettingResolverService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Data
class MapConf {

  private String hardwareIds;
  private String[] tags;
}

@Log
@Component
@RequiredArgsConstructor
public class MapResolver implements WidgetValueResolver {

  private final ObjectMapper mapper;
  private final DeviceService deviceService;
  private final SettingResolverService srs;

  private boolean coordsExist(DevicePageDTO lon, DevicePageDTO lat) {
    return (lon != null && lon.getValue() != null && StringUtils
      .isNotEmpty(lon.getValue().toString())
      && lat != null && lat.getValue() != null && StringUtils
      .isNotEmpty(lat.getValue().toString()));
  }

  @Override
  public List<DeviceCoordinates> getValue(DashboardWidget widget) {
    try {
      MapConf conf = mapper.readValue(widget.getConfiguration(), MapConf.class);

      // Check if lon/lat setting is specified.
      String latSetting = srs.get(Device.LATITUDE);
      String lonSetting = srs.get(Device.LONGITUDE);

      // If no settings exist for lat/lot, return an empty reply.
      if (StringUtils.isEmpty(latSetting) || StringUtils.isEmpty(latSetting)) {
        log.warning("Requested device coordinates, however values for latitude and longitue have "
          + "not been set in application settings.");
        return new ArrayList<>();
      }

      // Collect all devices for which coordinates need to be fetched.
      List<DeviceCoordinates> coordinates = new ArrayList<>();
      ListUtils.union(
        deviceService.findByHardwareIds(Arrays.asList(conf.getHardwareIds().split(","))),
        deviceService.findByTags(conf.getTags())
      ).stream()
        .distinct()
        .forEach(device -> {
          DevicePageDTO lon =
            deviceService.getDeviceDataField(device.getId(), lonSetting);
          DevicePageDTO lat =
            deviceService.getDeviceDataField(device.getId(), latSetting);

          if (coordsExist(lon, lat)) {
            DeviceCoordinates deviceCoordinates = new DeviceCoordinates();
            deviceCoordinates.setId(device.getId());
            deviceCoordinates.setHardwareId(device.getHardwareId());
            deviceCoordinates.setLon(new BigDecimal(lon.getValue().toString()));
            deviceCoordinates.setLat(new BigDecimal(lat.getValue().toString()));
            // Use longitude to find last updated on value (considering lat/lon get updated
            // simultaneously).
            deviceCoordinates.setCoordinatesUpdatedOn(
              deviceService.getDeviceDataField(device.getId(), lonSetting).getLastUpdatedOn());
            coordinates.add(deviceCoordinates);
          }
        });
      return coordinates;
    } catch (JsonProcessingException e) {
      throw new QMismatchException("Could not obtain configuration for widget Id ''{0}''",
        widget.getId());
    }
  }
}
