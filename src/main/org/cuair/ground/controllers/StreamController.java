package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ImageDatabaseAccessor;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.geotag.GimbalOrientation;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.util.Flags;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.InterruptedException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaAudio;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

/** Contains all the callbacks for all the public api endpoints for the Image */
@CrossOrigin
@RestController
@RequestMapping(value = "/stream")
public class StreamController {
  /** String path to the folder where all the images are stored */
  private String streamImageDir = Flags.STREAM_IMAGE_DIR;

  private Integer extension = 0;
  private Integer numImg = 0;
  private Muxer muxer;
  private Encoder encoder;
  private MediaPacket packet;
  private MediaPicture picture;
  private PixelFormat.Type pixelformat;
  private Rational framerate;
  private MediaPictureConverter converter;

  private void setupMuxer() throws InterruptedException, IOException {
  	/** First we create a muxer using the passed in filename and formatname if given. */
		muxer = Muxer.make("testVideo_" + extension + ".mp4", null, "mp4");

		/** Now, we need to decide what type of codec to use to encode video. Muxers
     * have limited sets of codecs they can use. We're going to pick the first one that
     * works, or if the user supplied a codec name, we're going to force-fit that
     * in instead.
     */
    final MuxerFormat format = muxer.getFormat();
    final Codec codec = Codec.findEncodingCodecByName("mpeg4");
    framerate = Rational.make(1, 24);

    /**
     * Now that we know what codec, we need to create an encoder
     */
    encoder = Encoder.make(codec);

    /**
     * Video encoders need to know at a minimum:
     *   width
     *   height
     *   pixel format
     * Some also need to know frame-rate (older codecs that had a fixed rate at which video files could
     * be written needed this). There are many other options you can set on an encoder, but we're
     * going to keep it simpler here.
     */
    encoder.setWidth(1280);
    encoder.setHeight(720);
    // We are going to use 420P as the format because that's what most video formats these days use
		pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
    encoder.setPixelFormat(pixelformat);
    encoder.setTimeBase(framerate);

    /** An annoynace of some formats is that they need global (rather than per-stream) headers,
     * and in that case you have to tell the encoder. And since Encoders are decoupled from
     * Muxers, there is no easy way to know this beyond
     */
    if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
      encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);

    /** Open the encoder. */
    encoder.open(null, null);

    /** Add this stream to the muxer. */
    muxer.addNewStream(encoder);

    /** And open the muxer for business. */
    muxer.open(null, null);

    /** Next, we need to make sure we have the right MediaPicture format objects
     * to encode data with. Java (and most on-screen graphics programs) use some
     * variant of Red-Green-Blue image encoding (a.k.a. RGB or BGR). Most video
     * codecs use some variant of YCrCb formatting. So we're going to have to
     * convert. To do that, we'll introduce a MediaPictureConverter object later. object.
     */
    MediaPictureConverter converter = null;
    picture = MediaPicture.make(
        encoder.getWidth(),
        encoder.getHeight(),
        pixelformat);
    picture.setTimeBase(framerate);

    packet = MediaPacket.make();
  }

  @PostConstruct
	public void initialize() throws InterruptedException, IOException {
		setupMuxer();
	}

	@PreDestroy
  public void preDestroy() {
  	/** Encoders, like decoders, sometimes cache pictures so it can do the right key-frame optimizations.
     * So, they need to be flushed as well. As with the decoders, the convention is to pass in a null
     * input until the output is not complete.
     */
  	System.out.println("YERRRD");
    do {
      encoder.encode(packet, null);
      if (packet.isComplete())
        muxer.write(packet, false);
    } while (packet.isComplete());

    /** Finally, let's clean up after ourselves. */
    muxer.close();
  }

  /**
   *
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity get() {
    return ok("We get it");
  }

  /**
   * Gets the imageFile from a valid body in the form of a File object.
   *
   * @param file the MultipartFile array of files
   * @return the image file
   */
  private File getImageFile(MultipartFile file) throws IOException {
    File convFile = new File(file.getOriginalFilename());
    convFile.createNewFile();
    FileOutputStream fos = new FileOutputStream(convFile);
    fos.write(file.getBytes());
    fos.close();
    return convFile;
  }

  /**
   * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of a
   * specified type. If the source image is the same type as the target type,
   * then original image is returned, otherwise new image of the correct type is
   * created and the content of the source image is copied into the new image.
   *
   * @param sourceImage
   *          the image to be converted
   * @param targetType
   *          the desired BufferedImage type
   *
   * @return a BufferedImage of the specifed target type.
   *
   * @see BufferedImage
   */
  public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
    BufferedImage image;

    // if the source image is already the target type, return the source image

    if (sourceImage.getType() == targetType) {
      image = sourceImage;
    }

    // otherwise create a new image of the target type and draw the new
    // image

    else
    {
      image = new BufferedImage(sourceImage.getWidth(),
          sourceImage.getHeight(), targetType);
      image.getGraphics().drawImage(sourceImage, 0, 0, null);
    }

    return image;
  }

  /**
   *
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity incoming(@RequestPart("file") MultipartFile file) throws InterruptedException, IOException {
    File imageFile;
    try {
      imageFile = getImageFile(file);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when parsing contentType for image file: \n" + e);
    }

    // store the image locally
    File newImageFile = FileUtils.getFile(streamImageDir + System.currentTimeMillis() + ".png");
    try {
      FileUtils.moveFile(imageFile, newImageFile);
    } catch (FileExistsException e) {
      return badRequest().body("File with timestamp already exists");
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error when moving image file: \n" + e);
    }


    // LOGIC FOR STARTING NEW MUX/VIDEO

    if ((numImg != 0) && (numImg % 24 == 0)) {
    	/** Encoders, like decoders, sometimes cache pictures so it can do the right key-frame optimizations.
	     * So, they need to be flushed as well. As with the decoders, the convention is to pass in a null
	     * input until the output is not complete.
	     */
	  	System.out.println("YERRRD");
	    do {
	      encoder.encode(packet, null);
	      if (packet.isComplete())
	        muxer.write(packet, false);
	    } while (packet.isComplete());

	    /** Finally, let's clean up after ourselves. */
	    muxer.close();

			/** Restart but with a new video segment */
			extension++;
			setupMuxer();
    }

    // Begin muxing
    BufferedImage image = ImageIO.read(newImageFile);
    final BufferedImage screen = convertToType(image, BufferedImage.TYPE_3BYTE_BGR);

    converter = MediaPictureConverterFactory.createConverter(screen, picture);
    converter.toPicture(picture, screen, System.currentTimeMillis());

    /** This is LIKELY not in YUV420P format, so we're going to convert it using some handy utilities. */
    // if (converter == null)
      // converter = MediaPictureConverterFactory.createConverter(screen, picture);
    // converter.toPicture(picture, screen, i);

    do {
      encoder.encode(packet, picture);
      if (packet.isComplete())
        muxer.write(packet, false);
    } while (packet.isComplete());

    System.out.println("muxed " + numImg);
    numImg++;
    return ok("We have received and saved the image");
  }
}
