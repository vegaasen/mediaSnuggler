package com.gtfo.snuggle.upnp;

import com.gtfo.snuggle.common.CommonConstantsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.common.CommonPartsOfMediaSnuggler;
import com.gtfo.snuggle.upnp.content.Content;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.*;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.support.connectionmanager.ConnectionManagerService;
import org.teleal.cling.support.xmicrosoft.AbstractMediaReceiverRegistrarService;

import java.net.URI;
import java.util.logging.Logger;

public class MediaServer {

    private static final Logger LOGGER = Logger.getLogger(MediaServer.class.getName());

    private final UDN uniqueDeviceName = UDN.uniqueSystemIdentifier(CommonConstantsOfMediaSnuggler.APPLICATION_NAME);
    private final Content content;

    public MediaServer(Content content) {
        LOGGER.info("Starting the first " + CommonConstantsOfMediaSnuggler.APPLICATION_NAME + " with content: " + content);
        this.content = content;
    }

    public UDN getUniqueDeviceName() {
        return uniqueDeviceName;
    }

    public Content getContent() {
        return content;
    }

    @SuppressWarnings("unchecked")
    public LocalDevice createDevice()
            throws ValidationException, LocalServiceBindingException {

        DeviceType type = new UDADeviceType("MediaServer", 1);

        DeviceDetails details = new DeviceDetails(
                CommonConstantsOfMediaSnuggler.APPLICATION_NAME, CommonPartsOfMediaSnuggler.MANUFACTOR, CommonPartsOfMediaSnuggler.MODEL_DETAILS);

        LocalService contentDirectory = new AnnotationLocalServiceBinder().read(ContentDirectory.class);
        contentDirectory.setManager(new DefaultServiceManager<ContentDirectory>(contentDirectory, null) {
                    @Override
                    protected ContentDirectory createServiceInstance() throws Exception {
                        return new ContentDirectory(getContent());
                    }
                }
        );

        LocalService connectionManager = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
        connectionManager.setManager(
                new DefaultServiceManager<ConnectionManagerService>(
                        connectionManager,
                        ConnectionManagerService.class
                )
        );
        LocalService[] activeServices = new LocalService[]{connectionManager, contentDirectory};
        return new LocalDevice(
                new DeviceIdentity(uniqueDeviceName), type, details, assembleDefaultIcon(), activeServices);
    }

    private static Icon assembleDefaultIcon() {
        return new Icon(CommonPartsOfMediaSnuggler.IMAGE_PNG, CommonPartsOfMediaSnuggler.WIDTH, CommonPartsOfMediaSnuggler.HEIGHT,
                CommonPartsOfMediaSnuggler.DEPTH, URI.create("icon.png"), CommonPartsOfMediaSnuggler.MEDIA_SNUGGLER_ICON);
    }

}
