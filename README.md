# Saud's Image Tools
Saud's Image Tools is a powerful, multithreaded image editing library written in pure Java,
designed to simplify image processing tasks while offering a wide array of advanced features.
Whether you're an image editing enthusiast or a developer looking to integrate robust image
processing capabilities into your project, Saud's Image Tools has you covered.

Key Features:
- Efficiency Unleashed: My library, wraps each image in a base object, like ```ImageRaster```. 
  This innovative approach ensures that chaining modifications on an image is lightning-fast.
  It creates a new layer that adapts the pixel values on-the-fly, making it exceptionally  
  efficient for all your image manipulation needs.

- Advanced Functionality at Your Fingertips: My library is not just simple to use; it
  also empowers you with advanced image editing capabilities. From blob detection and analysis
  to Laplacian pyramids, binary mask operations like erode and dilate,
  and customizable convolution with advanced configurations, Saud's Image Tools
  takes your image editing game to the next level.

- Extend Your Creativity: Each class in [Library Name] is highly extensible, allowing you
  to create your own custom image operations. Whether it's a unique filter or a specialized
  image transformation, you have the flexibility to tailor the library to your specific
  requirements.

# Basic Usage
Each image has a base object e.g. ```ImageRaster```. When making any modifications to an image,
it is wrapped in a new layer that modifies the value when an attempt is made to access the pixel.

Note: An image can always be converted back into an ```ImageRaster``` by calling ```image.evaluate();```

## Create an Image
```java
// Blank image
int width1 = 3, height1 = 4, channels1 = 1;
Image newImage = ImageRaster.create(width1, height1, channels1);

// Pre-populated array of values
int width2 = 3, height2 = 4, channels2 = 1;
byte[] values = new byte[]{ 1,2,3, 4,5,6, 7,8,9, 10,11,12 }
Image newImage = ImageRaster.create(width2, height2, channels2, values);

// From a BufferedImage
File file1 = new File("/path/to/file/image1.jpg");
BufferedImage bufferedImage = ImageIO.read(file1);
Image newImage = ImageRaster.create(bufferedImage);

// From a file
File file2 = new File("/path/to/file/image1.jpg");
Image newImage = ImageRaster.create(file2);
```

## Applying Operations
All operations shipped with this library are static methods and are located in
the ```Operations``` class.
```java
// multiply image values by integer
Image image2 = Operations.multiply(image, 2);

// add two images
Image image3 = Operations.add(image, image2);

// gaussian blur
Image gaussian = Operations.convolve(image, BorderHandling.INNER, Kernel.gaussian3x3(), Aggregator.MEAN, 1, 1);

// hot pixel removal
Image convolved = Operations.convolve(image, BorderHandling.IGNORE, Kernel.boxBlur(5), Aggregator.MEDIAN, 1, 1);
```

## Previewing and Saving Images
You can preview images without saving it to the disk using the ```Viewer``` class.
```java
// preview a pair of images
Viewer.showAll("Some Window Title", image1, image2);

// save an image
image.save("png", new File("/path/to/file.txt"))
```