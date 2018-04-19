#How to call example
#python get_face_vector.py -i test2.bmp

import optparse
import dlib
import pickle
from skimage import io
detector = dlib.get_frontal_face_detector()

def getAllFaceBoundingBoxes(rgbImg):
  assert rgbImg is not None
  try:
    return detector(rgbImg, 1)
  except Exception as e:
    print("Warning: {}".format(e))
    return []  

if __name__ == '__main__':
  parser = optparse.OptionParser()
  parser.add_option("-i", "--image", dest="image", default = 'test.bmp')
  sp = dlib.shape_predictor('shape_predictor_5_face_landmarks.dat')
  facerec = dlib.face_recognition_model_v1('dlib_face_recognition_resnet_model_v1.dat')
  options, _ = parser.parse_args()
  f = options.image
  img = io.imread(f)
  dets = getAllFaceBoundingBoxes(img)
  # print("Number of faces detected: {}".format(len(dets)))
  # for i, d in enumerate(dets):
  #   shape = sp(img, d)
  #   v = facerec.compute_face_descriptor(img, shape);


  faces = dlib.full_object_detections()
  for detection in dets:
    faces.append(sp(img, detection))

  images = dlib.get_face_chips(img, faces, size=150, padding=0.25)

  img = images[0]
  dets = getAllFaceBoundingBoxes(img)
  for i, d in enumerate(dets):
    shape = sp(img, d)
    v = facerec.compute_face_descriptor(img, shape);

  with open('face.vec', 'wb') as handle:
    pickle.dump(v, handle)